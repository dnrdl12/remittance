package io.dnrdl12.remittance.comm.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.dnrdl12.remittance.comm.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "공통 API 응답 래퍼")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BaseResponse<T> {

    @Schema(description = "응답 코드(내부 표준 코드)")
    private String code;

    @Schema(description = "응답 메시지")
    private String message;

    @Schema(description = "요청 경로")
    private String path;

    @Schema(description = "응답 시각")
    private LocalDateTime timestamp;

    @Schema(description = "응답 데이터")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder()
                .code(ResultCode.OK.getCode())
                .message(ResultCode.OK.getMessage())
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> ok(ResultCode code, T data) {
        return BaseResponse.<T>builder()
                .code(code.getCode())
                .message(code.getMessage())
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> fail(ResultCode code, String detailMessage) {
        return BaseResponse.<T>builder()
                .code(code.getCode())
                .message(detailMessage != null ? detailMessage : code.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public BaseResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }
}
