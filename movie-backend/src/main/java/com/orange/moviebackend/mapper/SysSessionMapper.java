package com.orange.moviebackend.mapper;

import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysSessionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysSessionMapper {

    List<SysSession> findByVo(SysSessionVo sysSessionVo);

    List<SysSession> findSessionByMovieIdOrHallId(SysSession sysSession);

    SysSession findSessionById(Long id);

    SysSession findOneSession(Long id);

    int addSession(SysSession sysSession);

    int updateSession(SysSession sysSession);

    int deleteSession(Long id);

    List<SysSession> findSessionByMovieId(Long movieId);
    /**
     * 【核心修复】
     * 更新场次的座位图和已售数量。
     * @param sessionId 场次ID
     * @param sessionSeatsJson 更新后的座位图 JSON 字符串
     * @param ticketCount 本次售出的票数
     */
    void updateSeatsAndSoldCount(
            @Param("sessionId") Long sessionId,
            @Param("sessionSeatsJson") String sessionSeatsJson,
            @Param("ticketCount") int ticketCount
    );

}
