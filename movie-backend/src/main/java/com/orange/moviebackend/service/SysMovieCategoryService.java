package com.orange.moviebackend.service;

import com.orange.moviebackend.domain.SysMovieCategory;
import com.orange.moviebackend.domain.SysMovieToCategory;

import java.util.List;

public interface SysMovieCategoryService {

    List<SysMovieCategory> findAllCategorys();

    SysMovieCategory findCategoryById(Long id);

    int addCategory(SysMovieCategory sysMovieCategory);

    int updateCategory(SysMovieCategory sysMovieCategory);

    int deleteCategory(Long[] ids);

    int addMovieToCategory(SysMovieToCategory sysMovieToCategory);

    int deleteMovieToCategory(SysMovieToCategory sysMovieToCategory);


}
