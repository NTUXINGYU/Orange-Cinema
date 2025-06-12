package com.orange.moviebackend.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Data
public class SysBill implements Serializable {

    private static final Long serialVersionUID = 1L;

    private Long billId;

    //订单状态，0表示未支付，1表示已完成
    private Boolean payState;

    //下订单的用户id
    @NotNull(message = "The user placing the order cannot be empty.")
    private Long userId;

    //订单所属的场次
    @NotNull(message = "The order cannot be empty.")
    private Long sessionId;

    //订单的座位，如：1排10号、A排5号
    @NotBlank(message = "The seats selected for the order cannot be empty.")
    private String seats;

    private Boolean cancelState;

    private Boolean cancelRole;

    private Date createTime;

    private Date payTime;

    private Date deadline;

    private Date cancelTime;

    private Double price;

    // 用户名作模糊查询条件
    private String queryByUserName;

    //多表连接
    private SysSession sysSession;

    private SysUser sysUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysBill sysBill = (SysBill) o;
        return Objects.equals(billId, sysBill.billId) && Objects.equals(payState, sysBill.payState) && Objects.equals(userId, sysBill.userId) && Objects.equals(sessionId, sysBill.sessionId) && Objects.equals(seats, sysBill.seats) && Objects.equals(cancelState, sysBill.cancelState) && Objects.equals(cancelRole, sysBill.cancelRole) && Objects.equals(createTime, sysBill.createTime) && Objects.equals(deadline, sysBill.deadline) && Objects.equals(cancelTime, sysBill.cancelTime) && Objects.equals(queryByUserName, sysBill.queryByUserName) && Objects.equals(sysSession, sysBill.sysSession) && Objects.equals(sysUser, sysBill.sysUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, payState, userId, sessionId, seats, cancelState, cancelRole, createTime, deadline, cancelTime, queryByUserName, sysSession, sysUser);
    }
}
