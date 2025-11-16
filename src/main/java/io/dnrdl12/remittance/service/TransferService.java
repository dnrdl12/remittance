package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.dto.TransferDto;
import io.dnrdl12.remittance.entity.Transfer;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : TransferService
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI              최초 생성
 */
public interface TransferService {

    Transfer deposit(TransferDto.DepositReq req);
    Transfer withdraw(TransferDto.WithdrawReq req);
    Transfer transfer(TransferDto.TransferReq req) ;
}