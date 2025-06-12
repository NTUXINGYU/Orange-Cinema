package com.orange.moviebackend.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewingStartVo {

    private String streamUrl;

    private String websocketUrl;

    private String ticket;

    private long syncTime;
}