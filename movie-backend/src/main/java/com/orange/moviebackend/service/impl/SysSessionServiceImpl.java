package com.orange.moviebackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.moviebackend.common.exception.BusinessException;
import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysSessionVo;
import com.orange.moviebackend.mapper.SysSessionMapper;
import com.orange.moviebackend.service.SysSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysSessionServiceImpl implements SysSessionService {

    @Resource
    private SysSessionMapper sysSessionMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SEAT_LOCK_PREFIX = "lock:seat:";

    @Override
    public List<SysSession> findByVo(SysSessionVo sysSessionVo) {
        return sysSessionMapper.findByVo(sysSessionVo);
    }

    @Override
    public List<SysSession> findSessionByMovieIdOrHallId(SysSession sysSession) {
        return sysSessionMapper.findSessionByMovieIdOrHallId(sysSession);
    }

    @Override
    public SysSession findSessionById(Long id) {
        return sysSessionMapper.findSessionById(id);
    }

    @Override
    public SysSession findOneSession(Long id) {
        return sysSessionMapper.findOneSession(id);
    }

    @Override
    public int addSession(SysSession sysSession) {
        return sysSessionMapper.addSession(sysSession);
    }

    @Override
    public int updateSession(SysSession sysSession) {
        return sysSessionMapper.updateSession(sysSession);
    }

    @Override
    public int deleteSession(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            rows += sysSessionMapper.deleteSession(id);
        }
        return rows;
    }

    @Override
    public List<SysSession> findSessionByMovieId(Long movieId) {
        return sysSessionMapper.findSessionByMovieId(movieId);
    }

    @Override
    public boolean isSessionAvailableForSale(SysSession session) {
        if (session == null || session.getSessionDate() == null || session.getPlayTime() == null) {
            return false;
        }
        try {
            String fullDateTimeStr = session.getSessionDate().toString() + "T" + session.getPlayTime();
            LocalDateTime playTime = LocalDateTime.parse(fullDateTimeStr);
            LocalDateTime stopSaleTime = playTime.minusMinutes(10);
            return LocalDateTime.now().isBefore(stopSaleTime);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyUpdateSeats(Long sessionId, List<String> seatsToUpdate) {
        if (seatsToUpdate == null || seatsToUpdate.isEmpty()) {
            return;
        }

        SysSession session = sysSessionMapper.findSessionByIdForUpdate(sessionId);
        if (session == null || !StringUtils.hasText(session.getSessionSeats())) {
            throw new BusinessException("Cannot update seats: session or seat map not found for ID " + sessionId);
        }

        try {
            Map<String, List<Integer>> seatMap = objectMapper.readValue(session.getSessionSeats(), new TypeReference<Map<String, List<Integer>>>() {});
            int newlySoldCount = 0;

            for (String seat : seatsToUpdate) {
                String[] parts = seat.split("-");
                String row = parts[0];
                int colIndex = Integer.parseInt(parts[1]) - 1;

                if (seatMap.containsKey(row) && colIndex >= 0 && colIndex < seatMap.get(row).size()) {
                    if (seatMap.get(row).get(colIndex) != 3) {
                        seatMap.get(row).set(colIndex, 3);
                        newlySoldCount++;
                    } else {
                        System.out.println("Warning: Seat " + seat + " for session " + sessionId + " was already marked as sold.");
                    }
                } else {
                    throw new BusinessException("Seat " + seat + " does not exist in the seat map for session " + sessionId);
                }
            }

            if (newlySoldCount > 0) {
                String updatedSeatsJson = objectMapper.writeValueAsString(seatMap);
                sysSessionMapper.updateSeatsAndSoldCount(sessionId, updatedSeatsJson, newlySoldCount);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process seat map JSON for session " + sessionId, e);
        }
    }

    @Override
    public Set<String> getTemporarilyLockedSeats(Long sessionId) {
        Set<String> keys = redisTemplate.keys(SEAT_LOCK_PREFIX + sessionId + ":*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        final String prefixToRemove = SEAT_LOCK_PREFIX + sessionId + ":";
        return keys.stream()
                .map(key -> key.substring(prefixToRemove.length()))
                .collect(Collectors.toSet());
    }
}