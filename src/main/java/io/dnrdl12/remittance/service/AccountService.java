package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.dto.AccountDto;
import io.dnrdl12.remittance.comm.api.PageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : AccountService
 * author         : JW.CHOI
 * date           : 2025-11-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-14        JW.CHOI              최초 생성
 */

public interface AccountService {
    AccountDto.Res create(AccountDto.CreateReq req, String userId);
    Page<AccountDto.SearchSimpleRes> searchAccounts(AccountDto.SearchReq req, boolean masked);
    AccountDto.SearchDetailRes getAccountDetail(Long accountSeq);
    AccountDto.Res patch(Long accountSeq, AccountDto.PatchReq req, String userId);
    AccountDto.IdResponse deleteSoft(Long accountSeq, String userId);
}