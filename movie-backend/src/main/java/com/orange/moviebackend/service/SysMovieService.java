package com.orange.moviebackend.service;

import com.orange.moviebackend.domain.SysMovie;
import com.orange.moviebackend.domain.vo.SysMovieVo;

import java.util.List;


public interface SysMovieService {

    List<SysMovie> findAllMovies(SysMovieVo sysMovieVo);

    SysMovie findMovieById(Long id);

    SysMovie findOneMovie(Long id);

    int addMovie(SysMovie sysMovie);

    int updateMovie(SysMovie sysMovie);

    int deleteMovie(Long[] ids);

    List<SysMovie> NowPlayingMoviesList();

    List<SysMovie> UpcomingMoviesList();

    List<SysMovie> TopRatedMoviesList();
}
