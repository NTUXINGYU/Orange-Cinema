package com.orange.moviebackend.service.impl;

import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysSessionVo;
import com.orange.moviebackend.mapper.SysSessionMapper;
import com.orange.moviebackend.service.SysSessionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class SysSessionServiceImpl implements SysSessionService {

    @Resource
    private SysSessionMapper sysSessionMapper;

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

}
