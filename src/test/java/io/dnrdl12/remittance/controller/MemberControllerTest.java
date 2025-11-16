package io.dnrdl12.remittance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.dto.MemberDto;
import io.dnrdl12.remittance.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * packageName    : io.dnrdl12.remittance.controller
 * fileName       : MemberControllerTest
 * author         : JW.CHOI
 * date           : 2025-11-17
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-17        JW.CHOI              최초 생성
 */


@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("회원 등록 API - 성공")
    void createMember_success() throws Exception {
        // given
        MemberDto.CreateReq req = MemberDto.CreateReq.builder()
                .memberNm("홍길동")
                .memberPhone("01012345678")
                .memberCi("CI-TEST")
                .memberDi("DI-TEST")
                .privConsentYn("Y")
                .msgConsentYn("Y")
                .build();

        MemberDto.IdResponse idRes = MemberDto.IdResponse.of(1L);
        given(memberService.create(any(MemberDto.CreateReq.class), anyString()))
                .willReturn(idRes);

        // when & then
        mockMvc.perform(post("/api/member")
                        .header("X-USER-ID", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberSeq").value(1L));
    }

    @Test
    @DisplayName("회원 수정 API - 성공")
    void updateMember_success() throws Exception {
        // given
        MemberDto.UpdateReq req = MemberDto.UpdateReq.builder()
                .memberSeq(1L)
                .memberNm("수정홍길동")
                .build();

        MemberDto.IdResponse idRes = MemberDto.IdResponse.of(1L);
        given(memberService.update(any(MemberDto.UpdateReq.class), anyString()))
                .willReturn(idRes);

        // when & then
        mockMvc.perform(put("/api/member")
                        .header("X-USER-ID", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberSeq").value(1L));
    }

    @Test
    @DisplayName("회원 삭제 API - 성공")
    void deleteMember_success() throws Exception {
        // given
        MemberDto.IdResponse idRes = MemberDto.IdResponse.of(1L);
        given(memberService.deleteSoft(1L, "admin")).willReturn(idRes);

        // when & then
        mockMvc.perform(delete("/api/member/{memberSeq}", 1L)
                        .header("X-USER-ID", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberSeq").value(1L));
    }

    @Test
    @DisplayName("회원 상세 조회 API - 성공")
    void getMember_success() throws Exception {
        // given
        MemberDto.Res res = MemberDto.Res.builder()
                .memberSeq(1L)
                .memberNm("홍길동")
                .memberPhone("010****5678")
                .build();

        given(memberService.getById(1L, true)).willReturn(res);

        // when & then
        mockMvc.perform(get("/api/member/{memberSeq}", 1L)
                        .header("X-MASKED", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberSeq").value(1L))
                .andExpect(jsonPath("$.data.memberNm").value("홍길동"));
    }

    @Test
    @DisplayName("회원 목록 조회 API - 성공")
    void searchMembers_success() throws Exception {
        // given
        MemberDto.Res res1 = MemberDto.Res.builder()
                .memberSeq(1L)
                .memberNm("검색홍길동")
                .memberPhone("010****5678")
                .build();

        PageDto.PageInfo pageInfo = PageDto.PageInfo.builder()
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .first(true)
                .last(true)
                .numberOfElements(1)
                .empty(false)
                .build();
        PageDto.PageResponse<MemberDto.Res> pageRes = PageDto.PageResponse.of(List.of(res1), pageInfo);

        given(memberService.search(any(MemberDto.SearchReq.class), anyBoolean()))
                .willReturn(pageRes);

        // when & then
        mockMvc.perform(get("/api/member")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].memberSeq").value(1L))
                .andExpect(jsonPath("$.data.items[0].memberNm").value("검색홍길동"));
    }
}
