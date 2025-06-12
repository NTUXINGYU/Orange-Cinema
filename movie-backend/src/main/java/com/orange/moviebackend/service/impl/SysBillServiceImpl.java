package com.orange.moviebackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.moviebackend.common.exception.BusinessException;
import com.orange.moviebackend.common.exception.SeatLockException;
import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysBillVo;
import com.orange.moviebackend.mapper.SysBillMapper;
import com.orange.moviebackend.mapper.SysSessionMapper;
import com.orange.moviebackend.service.SysBillService;
import com.orange.moviebackend.service.SysSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysBillServiceImpl implements SysBillService {

    private static final Logger log = LoggerFactory.getLogger(SysBillServiceImpl.class);
    private static final String SEAT_LOCK_PREFIX = "lock:seat:";
    private static final Duration SEAT_LOCK_DURATION = Duration.ofMinutes(15);

    @Autowired private SysBillMapper sysBillMapper;
    @Autowired private SysSessionMapper sysSessionMapper;
    @Autowired private SysSessionService sysSessionService;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private ObjectMapper objectMapper;

    // 使用 Lua 脚本保证 Redis "检查并设置" 操作的原子性
    private static final RedisScript<Long> LOCK_SEATS_SCRIPT = new DefaultRedisScript<>(
            // 1. 检查所有 KEYS 是否存在，只要有一个存在，就立即返回 0 (失败)
            "for i = 1, #KEYS do if redis.call('exists', KEYS[i]) == 1 then return 0 end end; " +
                    // 2. 如果都不存在，则全部设置，并带上过期时间(PX 毫秒)
                    "for i = 1, #KEYS do redis.call('set', KEYS[i], ARGV[1], 'PX', ARGV[2]) end; " +
                    // 3. 返回 1 (成功)
                    "return 1",
            Long.class
    );

    @Override
    public List<SysBill> findByVo(SysBillVo sysBillVo) {
        return sysBillMapper.findByVo(sysBillVo);
    }

    @Override
    public SysBill findBillById(Long id) {
        return sysBillMapper.findBillById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysBill createBillAndLockSeats(SysBill bill) {
        if (bill.getSessionId() == null || !StringUtils.hasText(bill.getSeats())) {
            throw new BusinessException("Session ID and selected seats are required.");
        }

        List<String> selectedSeats = Arrays.asList(bill.getSeats().split(","));
        List<String> lockKeys = selectedSeats.stream()
                .map(seat -> SEAT_LOCK_PREFIX + bill.getSessionId() + ":" + seat)
                .collect(Collectors.toList());

        log.info("Phase 1: Attempting to acquire Redis lock for seats: {}", selectedSeats);
        Long redisLockResult = redisTemplate.execute(
                LOCK_SEATS_SCRIPT,
                lockKeys,
                bill.getUserId().toString(),
                String.valueOf(SEAT_LOCK_DURATION.toMillis())
        );

        if (redisLockResult == null || redisLockResult == 0) {
            log.warn("Redis lock failed for session {}. Seats might be taken.", bill.getSessionId());
            throw new SeatLockException("Sorry, some selected seats were just taken. Please choose again.");
        }
        log.info("Phase 1: Redis lock acquired successfully for seats: {}", selectedSeats);

        try {
            log.info("Phase 2: Acquiring database lock and verifying state for session: {}", bill.getSessionId());
            SysSession session = sysSessionMapper.findSessionByIdForUpdate(bill.getSessionId());
            if (session == null) throw new BusinessException("Session not found.");

            List<SysBill> existingBills = sysBillMapper.findUnpaidBillsForSeats(bill.getSessionId(), selectedSeats);
            if (!existingBills.isEmpty()) {
                throw new SeatLockException("Seats are already held in a pending order (database check).");
            }
            log.info("Phase 2: Database state verified. No conflicts found.");

            bill.setPayState(false);
            bill.setCancelState(false);
            bill.setCreateTime(new Date());
            bill.setDeadline(new Date(System.currentTimeMillis() + SEAT_LOCK_DURATION.toMillis()));
            sysBillMapper.addBill(bill);
            log.info("Phase 3: Created bill {} in database.", bill.getBillId());

            return bill;

        } catch (Exception e) {
            log.error("Error during database operation after acquiring Redis lock. Releasing Redis locks.", e);
            redisTemplate.delete(lockKeys);
            if (e instanceof BusinessException || e instanceof SeatLockException) throw e;
            throw new BusinessException("An internal error occurred while creating the order.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payBill(Long billId) {
        log.info("Processing payment for billId: {}", billId);
        SysBill bill = sysBillMapper.findBillByIdForUpdate(billId);
        if (bill == null) throw new BusinessException("Order does not exist.");
        if (bill.getPayState()) {
            log.warn("Bill {} is already paid. (Idempotent operation)", billId);
            return 1;
        }
        if (bill.getCancelState()) throw new BusinessException("Order has been cancelled and cannot be paid.");

        sysSessionService.permanentlyUpdateSeats(bill.getSessionId(), Arrays.asList(bill.getSeats().split(",")));
        log.info("Permanently updated seats in database for billId: {}", billId);

        bill.setPayState(true);
        bill.setPayTime(new Date());
        int result = sysBillMapper.updateBill(bill);

        releaseRedisSeatLocks(bill);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelBill(Long billId) {
        log.info("Cancelling billId: {}", billId);
        SysBill bill = sysBillMapper.findBillById(billId);
        if (bill == null) {
            log.warn("Attempted to cancel a non-existent bill with ID: {}", billId);
            return 0;
        }
        if (bill.getPayState() || bill.getCancelState()) {
            log.warn("Bill {} cannot be cancelled (already paid or cancelled).", billId);
            return 0;
        }

        bill.setCancelState(true);
        bill.setCancelTime(new Date());
        int result = sysBillMapper.updateBill(bill);

        releaseRedisSeatLocks(bill);

        return result;
    }

    private void releaseRedisSeatLocks(SysBill bill) {
        if (bill == null || !StringUtils.hasText(bill.getSeats())) return;
        List<String> lockKeys = Arrays.stream(bill.getSeats().split(","))
                .map(seat -> SEAT_LOCK_PREFIX + bill.getSessionId() + ":" + seat)
                .collect(Collectors.toList());

        if (!lockKeys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(lockKeys);
            log.info("Released {} Redis seat locks for billId: {}", deletedCount, bill.getBillId());
        }
    }

    @Override
    public int deleteBillForAdmin(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            rows += sysBillMapper.deleteBill(id);
        }
        return rows;
    }

    @Override
    public List<SysBill> findTimeoutBills() {
        return sysBillMapper.findTimeoutBill();
    }
}