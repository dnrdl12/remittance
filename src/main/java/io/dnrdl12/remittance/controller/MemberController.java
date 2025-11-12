package io.dnrdl12.remittance.controller;

import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.dto.MemberDto;
import io.dnrdl12.remittance.service.MemberService;
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


@Tag(name = "회원 관리", description = "회원정보 등록, 수정, 삭제, 조회 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 등록", description = "새로운 회원 정보를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "등록 성공",
            content = @Content(schema = @Schema(implementation = MemberDto.IdResponse.class)))
    @PostMapping
    public ResponseEntity<BaseResponse<MemberDto.IdResponse>> create(
            @Valid @RequestBody MemberDto.CreateReq req,
            @Parameter(description = "등록자 ID", example = "admin") @RequestHeader("X-USER-ID") String actorId
    ) {
        var idRes = memberService.create(req, actorId);
        return ResponseEntity.ok(BaseResponse.ok(idRes));
    }

    @Operation(summary = "회원 수정", description = "회원 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping
    public ResponseEntity<BaseResponse<MemberDto.IdResponse>> update(
            @Valid @RequestBody MemberDto.UpdateReq req,
            @Parameter(description = "수정자 ID", example = "admin") @RequestHeader("X-USER-ID") String actorId
    ) {
        var idRes = memberService.update(req, actorId);
        return ResponseEntity.ok(BaseResponse.ok(idRes));
    }

    @Operation(summary = "회원 삭제", description = "회원 정보를 논리적으로 삭제합니다. (상태=2)")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{memberSeq}")
    public ResponseEntity<BaseResponse<MemberDto.IdResponse>> delete(
            @Parameter(description = "회원 식별번호", example = "10001") @PathVariable Long memberSeq,
            @Parameter(description = "삭제자 ID", example = "admin") @RequestHeader("X-USER-ID") String actorId
    ) {
        var idRes = memberService.deleteSoft(memberSeq, actorId);
        return ResponseEntity.ok(BaseResponse.ok(idRes));
    }

    @Operation(summary = "회원 상세조회", description = "회원 정보를 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{memberSeq}")
    public ResponseEntity<BaseResponse<MemberDto.Res>> get(
            @Parameter(description = "회원 식별번호", example = "10001") @PathVariable Long memberSeq
    ) {
        var res = memberService.getById(memberSeq, false);
        return ResponseEntity.ok(BaseResponse.ok(res));
    }

    @Operation(summary = "회원 목록조회", description = "검색 조건과 페이징 정보로 회원 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PageDto.PageResponse.class)))
    @GetMapping
    public ResponseEntity<BaseResponse<PageDto.PageResponse<MemberDto.Res>>> search(
            @Valid MemberDto.SearchReq req
    ) {
        var res = memberService.search(req, true); // 목록은 마스킹 적용
        return ResponseEntity.ok(BaseResponse.ok(res));
    }
}
