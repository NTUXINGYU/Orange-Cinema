package com.orange.moviebackend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
public class SysCinema implements Serializable {

    private final static Long serialVersionUID = 1L;

    private Long cinemaId;

    @NotBlank(message = "影院名称不能为空")
    private String cinemaName;

    private String hallCategoryList;

    private String cinemaAddress;

    private String cinemaPhone;

    @JsonFormat(locale = "en_SG", timezone = "Asia/Singapore", pattern = "HH:mm")
    private String workStartTime;

    @JsonFormat(locale = "en_SG", timezone = "Asia/Singapore", pattern = "HH:mm")
    private String workEndTime;

    //当前影院上映的所有电影，规则：上映的电影指包括今天在内，未来5天有安排目标影片的场次
    private List<SysMovie> sysMovieList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysCinema sysCinema = (SysCinema) o;
        return Objects.equals(cinemaId, sysCinema.cinemaId) && Objects.equals(cinemaName, sysCinema.cinemaName) && Objects.equals(hallCategoryList, sysCinema.hallCategoryList) && Objects.equals(cinemaAddress, sysCinema.cinemaAddress) && Objects.equals(cinemaPhone, sysCinema.cinemaPhone) && Objects.equals(workStartTime, sysCinema.workStartTime) && Objects.equals(workEndTime, sysCinema.workEndTime) && Objects.equals(sysMovieList, sysCinema.sysMovieList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cinemaId, cinemaName, hallCategoryList, cinemaAddress, cinemaPhone, workStartTime, workEndTime, sysMovieList);
    }

}
