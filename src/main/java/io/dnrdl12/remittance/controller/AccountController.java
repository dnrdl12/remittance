package io.dnrdl12.remittance.controller;

import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.dto.AccountDto;
import io.dnrdl12.remittance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * packageName    : io.dnrdl12.remittance.controller
 * fileName       : AccountController
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI              최초 생성
 * 2025-11-16        JW.CHOI              스웨거를 위한 코멘트 정리
 */
@Tag(name = "계좌 관리", description = "계좌 생성/조회/수정/해지 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계좌 생성", description = "새 계좌를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공",
            content = @Content(schema = @Schema(implementation = AccountDto.Res.class)))
    @PostMapping
    public ResponseEntity<BaseResponse<AccountDto.Res>> create(
            @Valid @RequestBody AccountDto.CreateReq req,
            @Parameter(description = "등록자 ID", example = "admin") @RequestHeader("X-USER-ID") String userId
    ) {
        var res = accountService.create(req, userId);
        return ResponseEntity.ok(BaseResponse.ok(res));
    }

    @Operation(summary = "계좌 정보 수정 (별칭/상태)", description = "계좌 별칭 및 상태를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = AccountDto.Res.class)))
    @PatchMapping("/{seq}")
    public ResponseEntity<BaseResponse<AccountDto.Res>> patch(
            @PathVariable("seq") Long seq,
            @Valid @RequestBody AccountDto.PatchReq req,
            @Parameter(description = "수정자 ID", example = "admin") @RequestHeader("X-USER-ID") String userId
    ) {
        var res = accountService.patch(seq, req, userId);
        return ResponseEntity.ok(BaseResponse.ok(res));
    }

    @Operation(summary = "계좌 상세 조회", description = "계좌SEQ로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = AccountDto.SearchDetailRes.class)))
    @GetMapping("/{accountSeq}")
    public ResponseEntity<BaseResponse<AccountDto.SearchDetailRes>> getAccountDetail(
            @PathVariable Long accountSeq
    ) {
        var res = accountService.getAccountDetail(accountSeq);
        return ResponseEntity.ok(BaseResponse.ok(res));
    }

    @Operation(summary = "계좌 목록 조회(검색)", description = "회원명, 회원번호, 계좌번호, 계좌SEQ로 가변 검색합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = Page.class)))
    @GetMapping
    public ResponseEntity<BaseResponse<Page<AccountDto.SearchSimpleRes>>> searchAccounts(
            AccountDto.SearchReq req,
            @Parameter(description = "정보 마스크", example = "true") @RequestHeader(value = "X-MASKED", required = false, defaultValue = "true") boolean masked
    ) {
        Page<AccountDto.SearchSimpleRes> page = accountService.searchAccounts(req, masked);
        return ResponseEntity.ok(BaseResponse.ok(page));
    }

    @Operation(summary = "계좌 해지", description = "계좌를 소프트 삭제(해지) 처리합니다.")
    @ApiResponse(responseCode = "200", description = "해지 성공",
            content = @Content(schema = @Schema(implementation = AccountDto.IdResponse.class)))
    @DeleteMapping("/{accountSeq}")
    public ResponseEntity<BaseResponse<AccountDto.IdResponse>> delete(
            @PathVariable("accountSeq") Long accountSeq,
            @Parameter(description = "삭제자 ID", example = "admin") @RequestHeader("X-USER-ID") String userId
    ) {
        var idRes = accountService.deleteSoft(accountSeq, userId);
        return ResponseEntity.ok(BaseResponse.ok(idRes));
    }
}
