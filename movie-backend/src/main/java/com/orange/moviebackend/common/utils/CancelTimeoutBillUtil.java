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
@EnableScheduling // 确保 @Scheduled 注解能够生效
public class CancelTimeoutBillUtil {

    private static final Logger log = LoggerFactory.getLogger(CancelTimeoutBillUtil.class);

    @Resource
    private SysBillService sysBillService;


    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelTimeoutBillsJob() {
        log.info("Scheduled Task: Starting scan for timeout bills...");

        try {
            List<SysBill> timeoutBillList = sysBillService.findTimeoutBills();

            if (timeoutBillList == null || timeoutBillList.isEmpty()) {
                log.info("Scheduled Task: No timeout bills found this minute.");
                return;
            }

            log.warn("Scheduled Task: Found {} timeout bill(s) to process.", timeoutBillList.size());

            for (SysBill bill : timeoutBillList) {
                try {
                    sysBillService.cancelBill(bill.getBillId());
                    log.info("Scheduled Task: Successfully cancelled timeout bill with ID: {}", bill.getBillId());
                } catch (Exception e) {
                    log.error("Scheduled Task: Failed to cancel timeout bill with ID: {}. Reason: {}", bill.getBillId(), e.getMessage(), e);
                }
            }

            log.info("Scheduled Task: Finished processing {} timeout bill(s).", timeoutBillList.size());

        } catch (Exception e) {
            log.error("An unexpected error occurred during the 'cancel timeout bills' job.", e);
        }
    }
}