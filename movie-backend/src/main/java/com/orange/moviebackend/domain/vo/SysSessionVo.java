package com.orange.moviebackend.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class SysSessionVo implements Serializable {

    private Long hallId;

    private Long movieId;

    @JsonFormat(locale = "en_SG", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    public SysSessionVo() {
    }

    public SysSessionVo(Long hallId, Long movieId, LocalDate sessionDate) {
        this.hallId = hallId;
        this.movieId = movieId;
        this.sessionDate = sessionDate;
    }

}
