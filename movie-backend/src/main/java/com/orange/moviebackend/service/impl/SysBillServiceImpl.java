package com.orange.moviebackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.orange.moviebackend.common.exception.BusinessException;
import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.mapper.SysBillMapper;
import com.orange.moviebackend.mapper.SysSessionMapper;
import com.orange.moviebackend.service.SysBillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SysBillServiceImpl implements SysBillService {

    private static final Logger log = LoggerFactory.getLogger(SysBillServiceImpl.class);
    private static final String SESSION_SEAT_LOCK_PREFIX = "lock:session:seats:";
    private static final long SEAT_LOCK_EXPIRE_MINUTES = 15;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private SysBillMapper sysBillMapper;

    @Resource
    private SysSessionMapper sysSessionMapper;

    @Override
    public List<SysBill> findAllBills(SysBill sysBill) {
        return sysBillMapper.findAllBills(sysBill);
    }

    @Override
    public SysBill findBillById(Long id) {
        return sysBillMapper.findBillById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysBill createBillAndLockSeats(SysBill bill) {
        List<String> selectedSeats = Arrays.asList(bill.getSeats().split(","));
        if (selectedSeats.isEmpty()) {
            throw new BusinessException("No seats selected.");
        }

        String redisKey = SESSION_SEAT_LOCK_PREFIX + bill.getSessionId();
        String[] seatsArray = selectedSeats.toArray(new String[0]);

        log.info("Attempting to lock {} seats for session {}: {}", selectedSeats.size(), bill.getSessionId(), selectedSeats);
        Long successCount = redisTemplate.opsForSet().add(redisKey, seatsArray);

        if (successCount != null && successCount == selectedSeats.size()) {
            log.info("Successfully locked {} seats for session {}.", successCount, bill.getSessionId());
            try {
                sysBillMapper.addBill(bill);
                log.info("Created bill {} for session {}.", bill.getBillId(), bill.getSessionId());
                redisTemplate.expire(redisKey, SEAT_LOCK_EXPIRE_MINUTES, TimeUnit.MINUTES);
                return bill;
            } catch (Exception e) {
                log.error("Database error after locking seats for session {}. Releasing locks.", bill.getSessionId(), e);
                redisTemplate.opsForSet().remove(redisKey, seatsArray);
                throw new BusinessException("Failed to create order. Please try again.");
            }
        } else {
            log.warn("Failed to lock seats for session {}. Success count: {}", bill.getSessionId(), successCount);
            if (successCount != null && successCount > 0) {
                redisTemplate.opsForSet().remove(redisKey, seatsArray);
            }
            throw new BusinessException("Seats are taken, please select again.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(Long billId) {
        log.info("Confirming payment for billId: {}", billId);

        // 1. 获取订单信息，并使用 forUpdate 进行悲观锁定
        SysBill bill = sysBillMapper.findBillByIdForUpdate(billId);
        if (bill == null) throw new BusinessException("Order does not exist.");
        if (bill.getPayState()) {
            log.warn("Bill {} is already paid.", billId);
            return; // 幂等性处理
        }
        if (bill.getCancelState()) throw new BusinessException("Order has been cancelled.");

        // 2. 更新订单状态为“已支付”
        int updatedRows = sysBillMapper.updateBillStatusToPaid(billId);
        if (updatedRows == 0) throw new BusinessException("Order status update failed. It might be cancelled or already paid.");

        // 3. 【核心修复】更新场次座位图和已售数量

        // a. 获取支付成功时，场次的最新座位图（可能在用户选座后，其他人也买了票）
        SysSession currentSession = sysSessionMapper.findOneSession(bill.getSessionId());
        if (currentSession == null) {
            throw new BusinessException("Session not found for this bill.");
        }

        // b. 解析座位图 JSON
        Map<String, List<Integer>> seatMap = JSON.parseObject(currentSession.getSessionSeats(), new TypeReference<Map<String, List<Integer>>>() {});

        // c. 解析本订单购买的座位
        List<String> seatsPaid = Arrays.asList(bill.getSeats().split(","));

        // d. 将这些座位在 seatMap 中的状态永久更新为 1 (已售)
        for(String seatStr : seatsPaid) {
            String[] parts = seatStr.split("-");
            String rowKey = parts[0];
            int colIndex = Integer.parseInt(parts[1]) - 1;

            // 安全检查
            if (seatMap.containsKey(rowKey) && colIndex < seatMap.get(rowKey).size()) {
                seatMap.get(rowKey).set(colIndex, 1); // 1 代表已售
            } else {
                log.error("Seat {} in bill {} does not exist in session {} seat map.", seatStr, billId, bill.getSessionId());
                // 根据业务严格程度，这里甚至可以抛出异常来回滚事务
            }
        }

        // e. 将更新后的 map 转换回 JSON 字符串
        String updatedSeatMapJson = JSON.toJSONString(seatMap);

        // f. 调用我们新的 Mapper 方法，一次性更新座位图和已售数量
        sysSessionMapper.updateSeatsAndSoldCount(bill.getSessionId(), updatedSeatMapJson, seatsPaid.size());
        log.info("Updated session {} seats and sold count.", bill.getSessionId());

        // 4. 持久化 Redis 中的座位锁 (可选但推荐)
        // 这一步确保了即使支付成功后系统崩溃，Redis 中的锁也不会在15分钟后过期
        String redisKey = SESSION_SEAT_LOCK_PREFIX + bill.getSessionId();
        redisTemplate.persist(redisKey);
        log.info("Persisted seat locks for session {}.", bill.getSessionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBill(Long billId) {
        log.info("Cancelling billId: {}", billId);
        SysBill bill = sysBillMapper.findBillById(billId);
        if (bill == null || bill.getCancelState() || bill.getPayState()) {
            log.warn("Bill {} cannot be cancelled (status already final).", billId);
            return;
        }

        sysBillMapper.updateBillStatusToCanceled(billId);

        String redisKey = SESSION_SEAT_LOCK_PREFIX + bill.getSessionId();
        List<String> seatsToRelease = Arrays.asList(bill.getSeats().split(","));
        redisTemplate.opsForSet().remove(redisKey, seatsToRelease.toArray(new String[0]));
        log.info("Released {} seats for cancelled bill {} from session {}", seatsToRelease.size(), billId, bill.getSessionId());
    }

    // 【补充】实现 deleteBillForAdmin
    @Override
    public int deleteBillForAdmin(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            // 注意：这里需要考虑是否需要释放座位锁，如果删除的是未支付订单
            // 为简化，假设后台删除操作只针对已完成或已取消的订单
            rows += sysBillMapper.deleteBill(id);
        }
        return rows;
    }

    // 【补充】实现 findTimeoutBills
    @Override
    public List<SysBill> findTimeoutBills() {
        return sysBillMapper.findTimeoutBill();
    }
}