package com.orange.moviebackend.service;

import com.orange.moviebackend.common.exception.ViewingException;
import com.orange.moviebackend.domain.vo.ViewingStartVo;

public interface OnlineViewingService {
    ViewingStartVo startViewing(Long billId, Long userId) throws ViewingException;

    boolean validateTicket(String ticket);

}