package com.orange.moviebackend.service;

import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysSessionVo;

import java.util.List;
import java.util.Set; // 引入Set

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

    /**
     * [核心新增]
     * 永久性地更新场次的座位状态。
     * 这个方法应该在支付成功后被调用，它包含了将座位标记为“已售出”的业务逻辑。
     * @param sessionId 场次ID
     * @param seatsToUpdate 需要被标记为“已售出”的座位列表 (例如 ["5-7", "5-8"])
     */
    void permanentlyUpdateSeats(Long sessionId, List<String> seatsToUpdate);

    /**
     * [新增]
     * 获取指定场次在 Redis 中所有被临时锁定的座位。
     * @param sessionId 场次ID
     * @return 被锁定座位的集合, 例如 {"5-7", "5-8"}
     */
    Set<String> getTemporarilyLockedSeats(Long sessionId);
}