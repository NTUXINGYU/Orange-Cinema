package com.orange.moviebackend.controller;

import com.orange.moviebackend.common.response.R;
import com.orange.moviebackend.common.utils.ApplicationContextUtils;
import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysSessionVo;
import com.orange.moviebackend.service.SysSessionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SysSessionController extends BaseController {

    @Resource
    private SysSessionService sysSessionService;

    /**
     * 查询所有场次 带查询条件
     *
     * @param sysSessionVo 场次信息
     * @return 结果
     */
    @GetMapping("/sysSession")
    public R findByQuery(SysSessionVo sysSessionVo) {
        startPage();
        List<SysSession> list = sysSessionService.findByVo(sysSessionVo);
        return getResult(list);
    }

    /**
     * 通过id查询场次
     *
     * @param id 场次id
     * @return 结果
     */
    @GetMapping("/sysSession/find/{id}")
    public R findSessionById(@PathVariable Long id) {
        return getResult(sysSessionService.findSessionById(id));
    }

    /**
     * 通过电影id,排座id查询场次
     *
     * @param sysSession 场次信息
     * @return 结果
     */
    @GetMapping("/sysSession/isAbleEdit")
    public R findSessionByMovieIdOrHallId(SysSession sysSession) {
        return getResult(sysSessionService.findSessionByMovieIdOrHallId(sysSession));
    }

    /**
     * 添加场次
     *
     * @param sysSession 场次信息
     * @return 结果
     */
    @PostMapping("/sysSession")
    public R addSession(@RequestBody SysSession sysSession) {
        return getResult(sysSessionService.addSession(sysSession));
    }

    /**
     * 更新场次
     *
     * @param sysSession 场次信息
     * @return 结果
     */
    @PutMapping("/sysSession")
    public R updateSession(@RequestBody SysSession sysSession) {
        return getResult(sysSessionService.updateSession(sysSession));
    }

    /**
     * 删除场次
     *
     * @param ids 场次id
     * @return 结果
     */
    @DeleteMapping("/sysSession/{ids}")
    public R deleteSession(@PathVariable Long[] ids) {
        return getResult(sysSessionService.deleteSession(ids));
    }


    @GetMapping("/isOnSale/{sessionId}")
    public R checkIfSessionOnSale(@PathVariable Long sessionId) {
        SysSession session = sysSessionService.findSessionById(sessionId);
        if (session == null) {
            return R.error("The event does not exist");
        }
        boolean isOnSale = sysSessionService.isSessionAvailableForSale(session);
        Map<String, Object> result = new HashMap<>();
        result.put("onSale", isOnSale);
        return R.success("Query successful", result);
    }

    @GetMapping("/sysSession/{movieId}")
    public R getSessionsByMovieId(@PathVariable Long movieId) {
        List<SysSession> sessions = sysSessionService.findSessionByMovieId(movieId);
        return R.success("Query successful", sessions);
    }

}
