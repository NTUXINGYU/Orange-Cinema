package com.orange.moviebackend.domain.vo;

import com.orange.moviebackend.domain.SysBill;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysBillVo implements Serializable {

    //订单信息
    private SysBill sysBill;

    //若成功更新后场次的座位信息
    private String sessionSeats;

}
