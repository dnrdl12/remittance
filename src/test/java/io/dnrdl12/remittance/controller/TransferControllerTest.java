package io.dnrdl12.remittance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.comm.enums.ClientId;
import io.dnrdl12.remittance.comm.enums.TransferStatus;
import io.dnrdl12.remittance.dto.TransferDto;
import io.dnrdl12.remittance.entity.Transfer;
import io.dnrdl12.remittance.service.TransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * packageName    : io.dnrdl12.remittance.controller
 * fileName       : TransferControllerTest
 * author         : JW.CHOI
 * date           : 2025-11-17
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-17        JW.CHOI              최초 생성
 */

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    private Transfer createDummyTransfer(Long seq) {
        Transfer t = new Transfer();
        // 필요한 필드만 세팅 (DTO.from에서 사용하는 필드 위주로)
        t.setTransferSeq(seq);
        t.setAmount(10_000L);
        t.setFee(100L);
        t.setCurrency("KRW");
        t.setStatus(TransferStatus.valueOf("SUCCESS"));
        t.setRequestedDate(LocalDateTime.now());
        return t;
    }

    @Test
    @DisplayName("입금 API - 성공")
    void deposit_success() throws Exception {
        // given
        TransferDto.DepositReq req = TransferDto.DepositReq.builder()
                .accountNumber("999-0000-000001")
                .amount(10_000L)
                .clientId(ClientId.valueOf("WEB"))
                .idempotencyKey("IDEMP-1")
                .build();

        Transfer transfer = createDummyTransfer(1L);
        given(transferService.deposit(any(TransferDto.DepositReq.class)))
                .willReturn(transfer);

        // when & then
        mockMvc.perform(post("/api/transfers/deposit")
                        .header("X-USER-ID", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transferSeq").value(1L))
                .andExpect(jsonPath("$.data.amount").value(10_000L));
    }

    @Test
    @DisplayName("출금 API - 성공")
    void withdraw_success() throws Exception {
        // given
        TransferDto.WithdrawReq req = TransferDto.WithdrawReq.builder()
                .accountNumber("999-0000-000001")
                .amount(5_000L)
                .clientId(ClientId.valueOf("WEB"))
                .idempotencyKey("IDEMP-2")
                .build();

        Transfer transfer = createDummyTransfer(2L);
        transfer.setAmount(5_000L);

        given(transferService.withdraw(any(TransferDto.WithdrawReq.class)))
                .willReturn(transfer);

        // when & then
        mockMvc.perform(post("/api/transfers/withdraw")
                        .header("X-USER-ID", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transferSeq").value(2L))
                .andExpect(jsonPath("$.data.amount").value(5_000L));
    }

    @Test
    @DisplayName("이체 API - 성공")
    void transfer_success() throws Exception {
        // given
        TransferDto.TransferReq req = TransferDto.TransferReq.builder()
                .fromAccountNumber("999-0000-000001")
                .toAccountNumber("999-0000-000002")
                .amount(20_000L)
                .clientId(ClientId.valueOf("WEB"))
                .idempotencyKey("IDEMP-3")
                .build();

        Transfer transfer = createDummyTransfer(3L);
        transfer.setAmount(20_000L);

        given(transferService.transfer(any(TransferDto.TransferReq.class)))
                .willReturn(transfer);

        // when & then
        mockMvc.perform(post("/api/transfers")
                        .header("X-USER-ID", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transferSeq").value(3L))
                .andExpect(jsonPath("$.data.amount").value(20_000L));
    }
}
