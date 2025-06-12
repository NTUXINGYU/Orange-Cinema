package com.orange.moviebackend.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserVo implements Serializable {

    private String userName;

    private String password;
}
