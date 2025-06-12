package com.orange.moviebackend.service.impl;

import com.orange.moviebackend.domain.SysCinema;
import com.orange.moviebackend.mapper.SysCinemaMapper;
import com.orange.moviebackend.service.SysCinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysCinemaServiceImpl implements SysCinemaService {

    @Autowired
    private SysCinemaMapper sysCinemaMapper;

    @Override
    public SysCinema findCinema() {
        return sysCinemaMapper.findCinema();
    }

    @Override
    public int updateCinema(SysCinema sysCinema) {
        return sysCinemaMapper.updateCinema(sysCinema);
    }

    @Override
    public SysCinema findCinemaById(Long id) {
        return sysCinemaMapper.findCinemaById(id);
    }
}
