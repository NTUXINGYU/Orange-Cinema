package com.orange.moviebackend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SysMovie implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long movieId;

    private String movieName;

    private Integer movieLength;

    private String moviePoster;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date releaseDate;

    private Double moviePopularity;

    private Double movieVoteAverage;

    private Integer movieVoteCount;

    private String movieIntroduction;

    private String movieTagline;

    private String movieDirector;

    private String movieGenres;

    private Integer delState;

    private List<SysMovieCategory> movieCategoryList;

    private String onlineUrl;

}