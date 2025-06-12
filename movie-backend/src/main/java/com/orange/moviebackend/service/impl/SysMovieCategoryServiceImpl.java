package com.orange.moviebackend.service.impl;

import com.orange.moviebackend.domain.SysMovieCategory;
import com.orange.moviebackend.domain.SysMovieToCategory;
import com.orange.moviebackend.mapper.SysMovieCategoryMapper;
import com.orange.moviebackend.service.SysMovieCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysMovieCategoryServiceImpl implements SysMovieCategoryService {

    @Resource
    private SysMovieCategoryMapper sysMovieCategoryMapper;

    @Override
    public List<SysMovieCategory> findAllCategorys() {
        return sysMovieCategoryMapper.findAllCategorys();
    }

    @Override
    public SysMovieCategory findCategoryById(Long id) {
        return sysMovieCategoryMapper.findCategoryById(id);
    }

    @Override
    public int addCategory(SysMovieCategory sysMovieCategory) {
        return sysMovieCategoryMapper.addCategory(sysMovieCategory);
    }

    @Override
    public int updateCategory(SysMovieCategory sysMovieCategory) {
        return sysMovieCategoryMapper.updateCategory(sysMovieCategory);
    }

    @Override
    public int deleteCategory(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            rows += sysMovieCategoryMapper.deleteCategory(id);
        }
        return rows;
    }


    @Override
    public int addMovieToCategory(SysMovieToCategory sysMovieToCategory) {
        return sysMovieCategoryMapper.addMovieToCategory(sysMovieToCategory);
    }

    @Override
    public int deleteMovieToCategory(SysMovieToCategory sysMovieToCategory) {
        return sysMovieCategoryMapper.deleteMovieToCategory(sysMovieToCategory);
    }

}
