package io.dnrdl12.remittance.comm.api;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * packageName    : io.dnrdl12.remittance.comm.api
 * fileName       : PageDto
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
public class PageDto {
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.experimental.SuperBuilder(builderMethodName = "pageBuilder") // ← 핵심
    @Schema(name = "PageDto.PageReq", description = "공통 페이징 요청")
    public static class PageReq {
        @Schema(description = "페이지(0-base)", example = "0", defaultValue = "0")
        @Min(0)
        private Integer page = 0;

        @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
        @Min(1)
        private Integer size = 20;

        @Schema(description = "정렬: 속성,ASC|DESC", example = "member_seq,DESC")
        private List<String> sort;
    }


    @Getter @Setter @NoArgsConstructor @AllArgsConstructor(staticName = "of") @Builder
    @Schema(name = "PageDto.PageInfo", description = "공통 페이지 메타")
    public static class PageInfo {
        private long totalElements;
        private int totalPages;
        private int page;  // 0-base
        private int size;  // page size
        private boolean first;
        private boolean last;
        private int numberOfElements;
        private boolean empty;


    }

    @Getter @AllArgsConstructor(staticName = "of")
    @Schema(name = "PageDto.PageResponse", description = "공통 페이징 응답")
    public static class PageResponse<T> {
        private final List<T> items;
        private final PageInfo page;
    }

    @Getter @AllArgsConstructor(staticName = "of")
    @Schema(name = "PageDto.ListResponse", description = "공통 리스트 응답(비페이징)")
    public static class ListResponse<T> {
        private final List<T> items;
    }

    @Getter @AllArgsConstructor(staticName = "of")
    @Schema(name = "PageDto.SliceResponse", description = "공통 슬라이스 응답(무한스크롤)")
    public static class SliceResponse<T> {
        private final List<T> items;
        private final boolean first;
        private final boolean last;
        private final int size;
        private final int numberOfElements;
        private final boolean hasNext;
    }
}