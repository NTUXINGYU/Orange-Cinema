package com.orange.moviebackend.common.utils;

import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.service.SysBillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@EnableScheduling
public class CancelTimeoutBillUtil {

    private static final Logger log = LoggerFactory.getLogger(CancelTimeoutBillUtil.class);

    @Resource
    private SysBillService sysBillService;

    /**
     * 定时扫描并取消超时订单。
     * 每分钟执行一次。
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutBillsJob() {
        log.info("Scheduled task: Scanning for timeout bills...");

        try {
            // 1. 调用 Service 层方法查询超时订单
            List<SysBill> timeoutBillList = sysBillService.findTimeoutBills();

            if (timeoutBillList == null || timeoutBillList.isEmpty()) {
                log.info("Scheduled task: No timeout bills found.");
                return;
            }

            log.warn("Scheduled task: Found {} timeout bill(s) to cancel.", timeoutBillList.size());

            // 2. 遍历列表，调用统一的取消服务
            for (SysBill bill : timeoutBillList) {
                try {
                    // 【核心】调用我们已经写好的、包含所有业务逻辑的 cancelBill 方法
                    sysBillService.cancelBill(bill.getBillId());
                    log.info("Scheduled task: Successfully cancelled timeout billId: {}", bill.getBillId());
                } catch (Exception e) {
                    log.error("Scheduled task: Failed to cancel timeout billId: {}. Reason: {}", bill.getBillId(), e.getMessage(), e);
                }
            }

            log.info("Scheduled task: Processed {} timeout bill(s).", timeoutBillList.size());

        } catch (Exception e) {
            log.error("An unexpected error occurred during the cancel timeout bills job.", e);
        }
    }
}