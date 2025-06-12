package com.orange.moviebackend.controller;

import com.orange.moviebackend.common.exception.BusinessException;
import com.orange.moviebackend.common.exception.SeatLockException;
import com.orange.moviebackend.common.response.R;
import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.domain.vo.SysBillVo;
import com.orange.moviebackend.service.SysBillService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单控制器，处理所有与订单相关的HTTP请求
 */
@RestController
@RequestMapping("/sysBill") // 将所有订单相关的操作聚合在 /sysBill 路径下
public class SysBillController extends BaseController {

    @Resource
    private SysBillService sysBillService;

    /**
     * 根据条件查询订单列表 (主要用于后台或“我的订单”)
     * GET /sysBill
     * @param sysBillVo 查询条件，可以包含 userId, payState 等
     * @return 分页后的订单列表
     */
    @GetMapping
    public R findByQuery(SysBillVo sysBillVo) {
        startPage();
        List<SysBill> list = sysBillService.findByVo(sysBillVo);
        return getResult(list);
    }

    /**
     * 根据ID查询单个订单详情
     * GET /sysBill/{id}
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public R findBillById(@PathVariable Long id) {
        SysBill bill = sysBillService.findBillById(id);
        return bill != null ? R.success(bill) : R.error("Order not found.");
    }


    @PostMapping
    public R createBill(@RequestBody SysBillVo sysBillVo) {
        // [核心微调] 从 VO 对象中获取 SysBill 实体
        SysBill billToCreate = sysBillVo.getSysBill();
        if (billToCreate == null) {
            return R.error("Order information is missing.");
        }

        try {
            SysBill createdBill = sysBillService.createBillAndLockSeats(billToCreate);
            return R.success("Order created successfully, please pay within 15 minutes.", createdBill);
        } catch (SeatLockException e) {
            return R.error(e.getMessage());
        } catch (BusinessException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error creating bill: " + e.getMessage());
            e.printStackTrace();
            return R.error("An unexpected error occurred while creating the order.");
        }
    }
    /**
     * [核心] 确认支付
     * POST /sysBill/pay/{id}
     * @param id 要支付的订单ID
     * @return 操作结果
     */
    @PostMapping("/pay/{id}")
    public R confirmPayment(@PathVariable Long id) {
        try {
            sysBillService.payBill(id);
            return R.success("Payment successful!");
        } catch (BusinessException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * [核心] 用户主动取消订单
     * POST /sysBill/cancel/{id}
     * @param id 要取消的订单ID
     * @return 操作结果
     */
    @PostMapping("/cancel/{id}")
    public R cancelBill(@PathVariable Long id) {
        try {
            sysBillService.cancelBill(id);
            return R.success("Order has been successfully cancelled.");
        } catch (BusinessException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 删除订单 (供后台管理使用)
     * DELETE /sysBill/{ids}
     * @param ids 订单ID数组
     * @return 操作结果
     */
    @DeleteMapping("/{ids}")
    public R deleteBillForAdmin(@PathVariable Long[] ids) {
        return getResult(sysBillService.deleteBillForAdmin(ids));
    }
}