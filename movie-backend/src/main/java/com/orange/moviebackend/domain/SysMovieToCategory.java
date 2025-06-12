package com.orange.moviebackend.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;


@Data
public class SysMovieToCategory implements Serializable {

    private static final Long serialVersionUID = 1L;

    private Long movieId;

    private Long movieCategoryId;


    public SysMovieToCategory() {
    }

    public SysMovieToCategory(Long movieId, Long movieCategoryId) {
        this.movieId = movieId;
        this.movieCategoryId = movieCategoryId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysMovieToCategory that = (SysMovieToCategory) o;
        return Objects.equals(movieId, that.movieId) && Objects.equals(movieCategoryId, that.movieCategoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, movieCategoryId);
    }

}
