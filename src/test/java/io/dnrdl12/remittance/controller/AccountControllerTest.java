package io.dnrdl12.remittance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.dto.AccountDto;
import io.dnrdl12.remittance.comm.enums.AccountStatus;
import io.dnrdl12.remittance.comm.enums.AccountType;
import io.dnrdl12.remittance.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * packageName    : io.dnrdl12.remittance.controller
 * fileName       : AccountControllerTest
 * author         : JW.CHOI
 * date           : 2025-11-17
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-17        JW.CHOI              최초 생성
 */

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 API - 성공")
    void createAccount_success() throws Exception {
        // given
        AccountDto.CreateReq req = AccountDto.CreateReq.builder()
                .memberSeq(1L)
                .nickname("테스트계좌")
                .accountType(AccountType.NORMAL)
                .feePolicySeq(1L)
                .build();

        AccountDto.Res res = AccountDto.Res.builder()
                .accountSeq(100L)
                .memberSeq(1L)
                .nickname("테스트계좌")
                .accountStatus(AccountStatus.NORMAL)
                .build();

        given(accountService.create(any(AccountDto.CreateReq.class), anyString()))
                .willReturn(res);

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .header("X-USER-ID", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountSeq").value(100L))
                .andExpect(jsonPath("$.data.nickname").value("테스트계좌"));
    }

    @Test
    @DisplayName("계좌 수정 API - 성공")
    void patchAccount_success() throws Exception {
        // given
        AccountDto.PatchReq req = AccountDto.PatchReq.builder()
                .nickname("수정계좌")
                .accountStatusCode(AccountStatus.SUSPENDED.getCode())
                .build();

        AccountDto.Res res = AccountDto.Res.builder()
                .accountSeq(100L)
                .memberSeq(1L)
                .nickname("수정계좌")
                .accountStatus(AccountStatus.SUSPENDED)
                .build();

        given(accountService.patch(anyLong(), any(AccountDto.PatchReq.class), anyString()))
                .willReturn(res);

        // when & then
        mockMvc.perform(patch("/api/accounts/{seq}", 100L)
                        .header("X-USER-ID", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountSeq").value(100L))
                .andExpect(jsonPath("$.data.nickname").value("수정계좌"))
                .andExpect(jsonPath("$.data.accountStatus").value(AccountStatus.SUSPENDED.name()));
    }

    @Test
    @DisplayName("계좌 상세 조회 API - 성공")
    void getAccountDetail_success() throws Exception {
        // given
        AccountDto.SearchDetailRes res = AccountDto.SearchDetailRes.builder()
                .accountSeq(100L)
                .accountNumber("123-456-7890")
                .nickname("상세계좌")
                .build();

        given(accountService.getAccountDetail(100L)).willReturn(res);

        // when & then
        mockMvc.perform(get("/api/accounts/{accountSeq}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountSeq").value(100L))
                .andExpect(jsonPath("$.data.accountNumber").value("123-456-7890"));
    }

    @Test
    @DisplayName("계좌 검색 API - 성공")
    void searchAccounts_success() throws Exception {
        // given
        AccountDto.SearchSimpleRes item = AccountDto.SearchSimpleRes.builder()
                .accountSeq(100L)
                .accountNumber("123-456-7890")
                .nickname("검색계좌")
                .memberNm("홍길동")
                .build();

        Page<AccountDto.SearchSimpleRes> page = new PageImpl<>(List.of(item));

        given(accountService.searchAccounts(any(AccountDto.SearchReq.class), anyBoolean()))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .header("X-MASKED", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].accountSeq").value(100L))
                .andExpect(jsonPath("$.data.content[0].accountNumber").value("123-456-7890"));
    }

    @Test
    @DisplayName("계좌 해지 API - 성공")
    void deleteAccount_success() throws Exception {
        // given
        AccountDto.IdResponse idRes = AccountDto.IdResponse.of(100L);
        given(accountService.deleteSoft(100L, "admin")).willReturn(idRes);

        // when & then
        mockMvc.perform(delete("/api/accounts/{accountSeq}", 100L)
                        .header("X-USER-ID", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountSeq").value(100L));
    }
}
