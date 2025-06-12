package com.orange.moviebackend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

@Data
public class SysSession implements Serializable {

    private static final Long serialVersionUID = 1L;

    // Session ID
    private Long sessionId;

    // Hall ID
    @NotNull(message = "Hall ID cannot be null")
    private Long hallId;

    private Double discountRate;

    // Movie ID
    @NotNull(message = "Movie ID cannot be null")
    private Long movieId;

    @JsonFormat(locale = "en_SG", timezone = "Asia/Singapore", pattern = "HH:mm")
    private String playTime;

    @JsonFormat(locale = "en_SG", timezone = "Asia/Singapore", pattern = "HH:mm")
    private String endTime;

    private String deadline;

    private String onlineUrl;

    private String movieDirector;

    @NotNull(message = "Session date cannot be null")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    // Session price
    @NotNull(message = "Session price cannot be null")
    @Size(min = 0, message = "Session price cannot be negative")
    private Double sessionPrice;

    // Session tips
    private String sessionTips;

    // Session seat information
    @NotBlank(message = "Session seat information cannot be blank")
    private String sessionSeats;

    private Integer seatNums;

    // Number of seats sold
    private Integer sallNums;

    private SysHall sysHall;

    private SysMovie sysMovie;

    private Integer delState;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysSession that = (SysSession) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(hallId, that.hallId) &&
                Objects.equals(discountRate, that.discountRate) &&
                Objects.equals(movieId, that.movieId) &&
                Objects.equals(playTime, that.playTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(deadline, that.deadline) &&
                Objects.equals(sessionDate, that.sessionDate) &&
                Objects.equals(sessionPrice, that.sessionPrice) &&
                Objects.equals(sessionTips, that.sessionTips) &&
                Objects.equals(sessionSeats, that.sessionSeats) &&
                Objects.equals(seatNums, that.seatNums) &&
                Objects.equals(sallNums, that.sallNums) &&
                Objects.equals(sysHall, that.sysHall) &&
                Objects.equals(sysMovie, that.sysMovie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, hallId, discountRate, movieId, playTime, endTime, deadline, sessionDate, sessionPrice, sessionTips, sessionSeats, seatNums, sallNums, sysHall, sysMovie);
    }

}
