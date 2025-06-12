package com.orange.moviebackend.service;

import com.orange.moviebackend.domain.SysBill;

import java.util.List;

public interface SysBillService {

    List<SysBill> findAllBills(SysBill sysBill);

    SysBill findBillById(Long id);

    SysBill createBillAndLockSeats(SysBill bill);

    void confirmPayment(Long billId);

    void cancelBill(Long billId);

    int deleteBillForAdmin(Long[] ids);

    // 方法名修正为复数形式，与实现保持一致
    List<SysBill> findTimeoutBills();
}