package com.moa.backend.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = """
        모든 API 에러 공통 응답 형식. `code` 값은 이 문서의 각 엔드포인트 설명에 적힌
        `(EXAMPLE_CODE)` 표기와 매칭된다 (예: `PROFILE_INCOMPLETE`, `PARTY_NOT_FOUND` 등).
        요청 바디 검증(`@Valid`) 실패 시에는 `code = INVALID_INPUT`이며 `fieldErrors`에
        어떤 필드가 왜 잘못됐는지가 채워진다.
        """)
public record ErrorResponse(
        @Schema(description = "에러 발생 시각") LocalDateTime timestamp,
        @Schema(description = "HTTP 상태 코드", example = "400") int status,
        @Schema(description = "서버 내부 에러 코드(ErrorCode enum 이름, 또는 검증 실패 시 INVALID_INPUT)", example = "PARTY_NOT_FOUND") String code,
        @Schema(description = "사용자에게 보여줘도 되는 한글 에러 메시지") String message,
        @Schema(description = "요청 바디 검증 실패(400) 응답에서만 채워지는 필드별 상세 오류 목록. 그 외에는 null.") List<FieldErrorDetail> fieldErrors
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(), errorCode.name(), message, null);
    }

    public static ErrorResponse of(int status, String code, String message, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, code, message, fieldErrors);
    }

    @Schema(description = "검증 실패한 단일 필드에 대한 상세 정보")
    public record FieldErrorDetail(
            @Schema(description = "검증에 실패한 요청 필드명", example = "capacity") String field,
            @Schema(description = "실패 사유(한글 메시지)", example = "정원은 2명 이상이어야 합니다.") String reason
    ) {
    }
}
