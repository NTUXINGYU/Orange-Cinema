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
     * [核心新增]
     * 根据ID查询场次，并施加行级锁 (FOR UPDATE)。
     * 这是防止在创建订单和支付时并发修改座位图的关键。
     * @param id 场次ID
     * @return 场次对象
     */
    SysSession findSessionByIdForUpdate(Long id);

    /**
     * [核心新增]
     * 一次性更新座位图JSON字符串和已售座位数。
     * @param sessionId 要更新的场次ID
     * @param sessionSeatsJson 新的、包含已售座位的JSON字符串
     * @param ticketCount 本次售出的票数，用于累加
     * @return 影响的行数
     */
    int updateSeatsAndSoldCount(@Param("sessionId") Long sessionId,
                                @Param("sessionSeatsJson") String sessionSeatsJson,
                                @Param("ticketCount") int ticketCount);

}