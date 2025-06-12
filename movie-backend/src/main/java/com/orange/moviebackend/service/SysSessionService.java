package com.orange.moviebackend.service;

import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysSessionVo;

import java.util.List;


public interface SysSessionService {

    List<SysSession> findByVo(SysSessionVo sysSessionVo);

    List<SysSession> findSessionByMovieIdOrHallId(SysSession sysSession);

    SysSession findSessionById(Long id);

    SysSession findOneSession(Long id);

    int addSession(SysSession sysSession);

    int updateSession(SysSession sysSession);

    int deleteSession(Long[] id);

    List<SysSession> findSessionByMovieId(Long movieId);

    boolean isSessionAvailableForSale(SysSession session);

}
