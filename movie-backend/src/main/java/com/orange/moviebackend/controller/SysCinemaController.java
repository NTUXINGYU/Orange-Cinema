package com.orange.moviebackend.controller;

import com.orange.moviebackend.common.response.R;
import com.orange.moviebackend.domain.SysCinema;
import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.service.SysCinemaService;
import com.orange.moviebackend.service.SysSessionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@RestController
public class SysCinemaController extends BaseController {

    @Resource
    private SysCinemaService sysCinemaService;

    @Resource
    private SysSessionService sysSessionService;

    /**
     * 查询影院
     *
     * @return 影院
     */
    @GetMapping("/sysCinema")
    public R findCinema() {
        return getResult(sysCinemaService.findCinema());
    }

    /**
     * 更新影院信息
     *
     * @param sysCinema 影院信息
     * @return 结果
     */
    @PutMapping("/sysCinema/update")
    public R updateCinema(@Validated @RequestBody SysCinema sysCinema) {
        return getResult(sysCinemaService.updateCinema(sysCinema));
    }

    /**
     * 通过id查询影院
     *
     * @param cinemaId 电影院id
     * @param movieId  电影id
     * @return 影院
     */
    @GetMapping(value = {"/sysCinema/find/{cinemaId}/{movieId}", "/sysCinema/find/{cinemaId}"})
    public R findCinemaById(@PathVariable Long cinemaId, @PathVariable(required = false) Long movieId) {
        SysCinema cinema = sysCinemaService.findCinemaById(cinemaId);
        if (movieId == null || movieId == 0) {
            movieId = cinema.getSysMovieList().size() > 0 ? cinema.getSysMovieList().get(0).getMovieId() : 0;
        }
        List<SysSession> sessions = null;
        if (movieId != null && movieId != 0) {
            sessions = sysSessionService.findSessionByMovieId(movieId);
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("cinema", cinema);
        response.put("sessions", sessions);
        return getResult(response);
    }

}
