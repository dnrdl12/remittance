package io.dnrdl12.remittance.controller;

import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.dto.TransferDto;
import io.dnrdl12.remittance.entity.Transfer;
import io.dnrdl12.remittance.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * packageName    : io.dnrdl12.remittance.controller
 * fileName       : TransferController
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI              최초 생성
 * 2025-11-16        JW.CHOI              스웨거를 위한 코멘트 정리
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers")
@Tag(name = "입출금 이체 관리", description = "입금/출금/이체 관련 API")
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "계좌 입금", description = "지정한 계좌로 금액을 입금합니다.")
    @ApiResponse(responseCode = "200", description = "입금 성공",
            content = @Content(schema = @Schema(implementation = TransferDto.TransferRes.class)))
    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<TransferDto.TransferRes>> deposit(
            @Valid @RequestBody TransferDto.DepositReq req,
            @Parameter(description = "요청자 ID", example = "admin") @RequestHeader(name = "X-USER-ID", required = false) String userId
    ) {
        Transfer transfer = transferService.deposit(req);
        var res = TransferDto.TransferRes.from(transfer);
        return ResponseEntity.ok(BaseResponse.ok(res));
    }

    @Operation(summary = "계좌 출금", description = "지정한 계좌에서 금액을 출금합니다. 출금 수수료 정책이 적용됩니다.")
    @ApiResponse(responseCode = "200", description = "출금 성공",
            content = @Content(schema = @Schema(implementation = TransferDto.TransferRes.class)))
    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<TransferDto.TransferRes>> withdraw(
            @Valid @RequestBody TransferDto.WithdrawReq req,
            @Parameter(description = "요청자 ID", example = "admin") @RequestHeader(name = "X-USER-ID", required = false) String userId
    ) {
        Transfer transfer = transferService.withdraw(req);
        var res = TransferDto.TransferRes.from(transfer);
        return ResponseEntity.ok(BaseResponse.ok(res));
    }

    @Operation(summary = "계좌간 이체", description = "출금 계좌에서 입금 계좌로 금액을 이체합니다. 이체 수수료 정책이 적용됩니다.")
    @ApiResponse(responseCode = "200", description = "이체 성공",
            content = @Content(schema = @Schema(implementation = TransferDto.TransferRes.class)))
    @PostMapping
    public ResponseEntity<BaseResponse<TransferDto.TransferRes>> transfer(
            @Valid @RequestBody TransferDto.TransferReq req,
            @Parameter(description = "요청자 ID", example = "admin") @RequestHeader(name = "X-USER-ID", required = false) String userId
    ) {
        Transfer transfer = transferService.transfer(req);
        var res = TransferDto.TransferRes.from(transfer);
        return ResponseEntity.ok(BaseResponse.ok(res));
    }
}
