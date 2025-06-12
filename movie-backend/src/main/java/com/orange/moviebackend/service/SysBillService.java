package com.orange.moviebackend.service;

import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.domain.vo.SysBillVo;
import java.util.List;

public interface SysBillService {

    List<SysBill> findByVo(SysBillVo sysBillVo);

    SysBill findBillById(Long id);

    SysBill createBillAndLockSeats(SysBill bill);

    int payBill(Long billId);

    int cancelBill(Long billId);

    int deleteBillForAdmin(Long[] ids);

    List<SysBill> findTimeoutBills();
}