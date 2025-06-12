package com.orange.moviebackend.controller;

import com.orange.moviebackend.common.exception.BusinessException;
import com.orange.moviebackend.common.response.R;
import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.domain.vo.SysBillVo; // 假设您有一个 SysBillVo
import com.orange.moviebackend.service.SysBillService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/sysBill") // 所有订单相关的操作都聚合在 /sysBill 路径下
public class SysBillController extends BaseController {

    @Resource
    private SysBillService sysBillService;

    /**
     * 根据条件查询订单列表 (主要用于后台或“我的订单”)
     * GET /sysBill
     * @param sysBill 查询条件，可以包含 userId, payState 等
     * @return 订单列表
     */
    @GetMapping
    public R findAllBills(SysBill sysBill) {
        startPage();
        List<SysBill> data = sysBillService.findAllBills(sysBill);
        return getResult(data);
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
        if (bill != null) {
            return R.success(bill);
        }
        return R.error("Order not found.");
    }

    /**
     * 【核心】创建订单并锁定座位
     * POST /sysBill
     * @param sysBillVo 包含订单核心信息的对象
     * @return 成功则返回新生成的订单ID，失败则返回错误信息
     */
    @PostMapping
    public R createBill(@RequestBody SysBillVo sysBillVo) {
        try {
            SysBill billToCreate = sysBillVo.getSysBill();
            SysBill createdBill = sysBillService.createBillAndLockSeats(billToCreate);

            return R.success("Order created...", createdBill.getBillId());
        } catch (BusinessException e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 【核心】确认支付
     * POST /sysBill/pay/{billId}
     * @param billId 要支付的订单ID
     * @return 操作结果
     */
    @PostMapping("/pay/{billId}")
    public R confirmPayment(@PathVariable Long billId) {
        try {
            sysBillService.confirmPayment(billId);
            return R.success("Payment successful!");
        } catch (BusinessException e) {
            // 捕获业务异常（如订单已支付/取消）
            return R.error(e.getMessage());
        }
    }

    /**
     * 【核心】取消订单
     * POST /sysBill/cancel/{billId}
     * @param billId 要取消的订单ID
     * @return 操作结果
     */
    @PostMapping("/cancel/{billId}")
    public R cancelBill(@PathVariable Long billId) {
        try {
            sysBillService.cancelBill(billId);
            return R.success("Order has been successfully cancelled.");
        } catch (BusinessException e) {
            // 捕获可能出现的业务异常
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