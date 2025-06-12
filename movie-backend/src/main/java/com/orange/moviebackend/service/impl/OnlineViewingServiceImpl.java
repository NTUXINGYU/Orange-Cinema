package com.orange.moviebackend.service.impl;

import com.orange.moviebackend.common.exception.ViewingException;
import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.domain.SysMovie;
import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.ViewingStartVo;
import com.orange.moviebackend.mapper.SysBillMapper;
import com.orange.moviebackend.mapper.SysMovieMapper;
import com.orange.moviebackend.mapper.SysSessionMapper;
import com.orange.moviebackend.service.OnlineViewingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OnlineViewingServiceImpl implements OnlineViewingService {

    private static final String LOCK_PREFIX = "lock:viewing:bill:";
    private static final String TICKET_PREFIX = "ticket:viewing:";
    // 将 Ticket 有效期设置为4小时
    private static final long TICKET_EXPIRATION_HOURS = 4;

    @Autowired
    private SysBillMapper billMapper;

    @Autowired
    private SysSessionMapper sessionMapper;

    @Autowired
    private SysMovieMapper movieMapper;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${websocket.server.url:ws://localhost:9231/ws/viewing}")
    private String websocketServerUrl;

    @Override
    public ViewingStartVo startViewing(Long billId, Long userId) throws ViewingException {
        if (redisTemplate == null) {
            throw new ViewingException("Online viewing service is temporarily unavailable. Redis is not configured.");
        }

        SysBill bill = billMapper.findBillById(billId);
        if (bill == null) throw new ViewingException("The order does not exist.");
        if (!bill.getUserId().equals(userId)) throw new ViewingException("You are not authorized to watch this movie.");
        if (bill.getCancelState() || !bill.getPayState()) throw new ViewingException("Order status is invalid.");

        String lockKey = LOCK_PREFIX + billId;
        String lockingUserIdStr = redisTemplate.opsForValue().get(lockKey);

        if (lockingUserIdStr != null && !lockingUserIdStr.equals(userId.toString())) {
            throw new ViewingException("This ticket is being watched on another device.");
        }

        redisTemplate.opsForValue().set(lockKey, userId.toString(), Duration.ofHours(TICKET_EXPIRATION_HOURS));

        try {
            SysSession session = sessionMapper.findSessionById(bill.getSessionId());
            if (session == null) throw new ViewingException("Session information does not exist.");

            SysMovie movie = movieMapper.findMovieById(session.getMovieId());
            String mediaPath = movie != null ? movie.getOnlineUrl() : null;
            if (!StringUtils.hasText(mediaPath)) throw new ViewingException("This movie is not available for online viewing.");

            LocalDateTime startTime = getCombinedStartTime(session);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = startTime.plusMinutes(movie.getMovieLength());

            if (now.isBefore(startTime)) {
                throw new ViewingException("Screening has not started yet. Please enter at " + startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + ".");
            }
            if (now.isAfter(endTime)) {
                throw new ViewingException("This session has ended.");
            }

            // 签发一个4小时有效期的临时 Ticket
            String ticket = UUID.randomUUID().toString();
            String ticketKey = TICKET_PREFIX + ticket;
            redisTemplate.opsForValue().set(ticketKey, billId.toString(), TICKET_EXPIRATION_HOURS, TimeUnit.HOURS);

            // 计算绝对进度
            long elapsedSeconds = Duration.between(startTime, now).getSeconds();
            String streamUrl = String.format("/live/ticket/%s/%s", ticket, mediaPath);
            return new ViewingStartVo(streamUrl, websocketServerUrl, ticket, elapsedSeconds);

        } catch (Exception e) {
            if (e instanceof ViewingException) {
                throw e;
            }
            e.printStackTrace();
            throw new ViewingException("Internal system error. Please try again later.");
        }
    }

    @Override
    public boolean validateTicket(String ticket) {
        if (redisTemplate == null || !StringUtils.hasText(ticket)) {
            return false;
        }
        String ticketKey = TICKET_PREFIX + ticket;
        Boolean hasKey = redisTemplate.hasKey(ticketKey);
        return hasKey != null && hasKey;
    }

    private LocalDateTime getCombinedStartTime(SysSession session) {
        if (session == null || session.getSessionDate() == null || !StringUtils.hasText(session.getPlayTime())) {
            throw new ViewingException("Session date or play time is incomplete.");
        }
        try {
            return session.getSessionDate().atTime(LocalTime.parse(session.getPlayTime(), DateTimeFormatter.ofPattern("HH:mm")));
        } catch (DateTimeParseException e) {
            throw new ViewingException("Play time format is incorrect.");
        }
    }
}