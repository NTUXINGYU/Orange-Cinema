package com.orange.moviebackend.domain;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@Data
public class SysHall implements Serializable {

    private final static Long serialVersionUID = 1L;

    private Long cinemaId;

    private Long hallId;

    @NotBlank(message = "影厅名称不能为空")
    private String hallName;

    private String hallCategory;

    //排开始标号：'1' / 'A' 等
    private String rowStart;

    //影厅排数
    @Min(value = 3, message = "排数不能小于3")
    @Max(value = 50, message = "排数不能大于50")
    private Integer rowNums;

    //每排座位数
    @Min(value = 3, message = "每排座位数不能小于3")
    @Max(value = 50, message = "每排座位数不能大于50")
    private Integer seatNumsRow;

    //总可用座位数，可以通过安排座位禁用指定座位
    @Min(value = 9, message = "座位数不能小于9")
    @Max(value = 2500, message = "座位数不能大于2500")
    private Integer seatNums;

    //座位的状态，使用json存储每个座位的状态，0表示可用，2表示禁用(1表示售出，在场次表中统计)
    private String seatState;

    private Boolean delState;

    //影厅表的多表连接
    private SysCinema sysCinema;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysHall sysHall = (SysHall) o;
        return Objects.equals(cinemaId, sysHall.cinemaId) && Objects.equals(hallId, sysHall.hallId) && Objects.equals(hallName, sysHall.hallName) && Objects.equals(hallCategory, sysHall.hallCategory) && Objects.equals(rowStart, sysHall.rowStart) && Objects.equals(rowNums, sysHall.rowNums) && Objects.equals(seatNumsRow, sysHall.seatNumsRow) && Objects.equals(seatNums, sysHall.seatNums) && Objects.equals(seatState, sysHall.seatState) && Objects.equals(delState, sysHall.delState) && Objects.equals(sysCinema, sysHall.sysCinema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cinemaId, hallId, hallName, hallCategory, rowStart, rowNums, seatNumsRow, seatNums,
                seatState, delState, sysCinema);
    }

}
