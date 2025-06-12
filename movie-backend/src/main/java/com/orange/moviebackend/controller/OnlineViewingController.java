package com.orange.moviebackend.controller;

import com.orange.moviebackend.common.config.shiro.JwtFilter;
import com.orange.moviebackend.common.exception.ViewingException;
import com.orange.moviebackend.common.response.R;
import com.orange.moviebackend.domain.vo.ViewingStartVo;
import com.orange.moviebackend.service.OnlineViewingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/viewing")
public class OnlineViewingController extends BaseController {

    @Autowired
    private OnlineViewingService onlineViewingService;

    @GetMapping("/start")
    public R startViewing(@RequestParam Long billId, HttpServletRequest request) {
        try {
            com.orange.moviebackend.domain.LoginUser loginUser =
                    (com.orange.moviebackend.domain.LoginUser) request.getAttribute(JwtFilter.USER_INFO_ATTRIBUTE);

            if (loginUser == null) {
                System.err.println("==================== DEBUG INFO ====================");
                System.err.println("CRITICAL: Failed to retrieve LoginUser from request attribute.");

                Object attribute = request.getAttribute(JwtFilter.USER_INFO_ATTRIBUTE);
                if (attribute != null) {
                    System.err.println("Attribute Key: " + JwtFilter.USER_INFO_ATTRIBUTE);
                    System.err.println("Attribute Value Type: " + attribute.getClass().getName());
                    System.err.println("Attribute Value (toString): " + attribute.toString());
                } else {
                    System.err.println("Attribute is COMPLETELY NULL.");
                }
                System.err.println("====================================================");

                return R.error(401, "User authentication context is lost. Please log in again.");
            }
            Long currentUserId = loginUser.getSysUser().getUserId();

            ViewingStartVo vo = onlineViewingService.startViewing(billId, currentUserId);
            return R.success("Successfully retrieved viewing information", vo);

        } catch (ViewingException e) {
            System.out.println("ViewingException occurred: " + e.getMessage());
            return R.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred in startViewing:");
            e.printStackTrace();
            return R.error("Internal server error. Please contact the administrator.");
        }
    }

    @GetMapping("/internal/validate")
    public ResponseEntity<?> validateTicketForNginx(@RequestParam(value = "ticket", required = false) String ticket) {
        boolean isValid = onlineViewingService.validateTicket(ticket);

        if (isValid) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}