package com.orange.moviebackend.service.impl;

import com.orange.moviebackend.domain.SysMovie;
import com.orange.moviebackend.domain.SysSession;
import com.orange.moviebackend.domain.vo.SysMovieVo;
import com.orange.moviebackend.mapper.SysMovieMapper;
import com.orange.moviebackend.mapper.SysSessionMapper;
import com.orange.moviebackend.service.SysMovieService;
import org.springframework.stereotype.Service;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class SysMovieServiceImpl implements SysMovieService {

    @Resource
    private SysMovieMapper sysMovieMapper;

    @Override
    public List<SysMovie> findAllMovies(SysMovieVo sysMovieVo) {
        return sysMovieMapper.findAllMovies(sysMovieVo);
    }

    @Override
    public SysMovie findMovieById(Long id) {
        return sysMovieMapper.findMovieById(id);
    }

    @Override
    public SysMovie findOneMovie(Long id) {
        return sysMovieMapper.findOneMovie(id);
    }

    @Override
    public int addMovie(SysMovie sysMovie) {
        if (sysMovie.getMovieId() != null && this.findMovieById(sysMovie.getMovieId()) != null) {
            throw new RuntimeException("The movie ID already exists and cannot be added again!");
        }
        return sysMovieMapper.addMovie(sysMovie);
    }

    @Override
    public int updateMovie(SysMovie sysMovie) {
        return sysMovieMapper.updateMovie(sysMovie);
    }

    @Override
    public int deleteMovie(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            rows += sysMovieMapper.deleteMovie(id);
        }
        return rows;
    }

    /*正在热映*/
    @Override
    public List<SysMovie> NowPlayingMoviesList() {
        return sysMovieMapper.NowPlayingMoviesList();
    }
    /*即将上映*/
    @Override
    public List<SysMovie> UpcomingMoviesList() {
        return sysMovieMapper.UpcomingMoviesList();
    }
    /*评分最高*/
    @Override
    public List<SysMovie> TopRatedMoviesList() {
        return sysMovieMapper.TopRatedMoviesList();
    }
}
