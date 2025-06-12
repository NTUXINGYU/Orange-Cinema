package com.orange.moviebackend.mapper;

import com.orange.moviebackend.domain.SysCinema;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysCinemaMapper {

    SysCinema findCinema();

    int updateCinema(SysCinema sysCinema);

    SysCinema findCinemaById(Long id);

}
