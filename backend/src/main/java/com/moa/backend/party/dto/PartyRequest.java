package com.moa.backend.party.dto;

import com.moa.backend.party.entity.StudyParty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "파티 생성/수정 요청. 생성(POST)과 수정(PUT) 모두 이 형식을 전체 필드 포함해서 보내야 한다.")
public record PartyRequest(
        @Schema(description = "파티 제목", example = "코딩테스트 스터디 3반 2조", maxLength = 100)
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Schema(description = "파티 소개(선택)", example = "매일 1문제씩 같이 풀어요.", maxLength = 2000, nullable = true)
        @Size(max = 2000, message = "소개는 2000자 이하여야 합니다.")
        String description,

        @Schema(description = "카테고리", example = "코딩테스트", maxLength = 30)
        @NotBlank(message = "카테고리는 필수입니다.")
        @Size(max = 30, message = "카테고리는 30자 이하여야 합니다.")
        String category,

        @Schema(description = "모집 정원 (2~100명). 승인된 파티원 수 기준으로 정원을 넘으면 참여 신청 수락이 거부된다.",
                example = "6", minimum = "2", maximum = "100")
        @NotNull(message = "정원은 필수입니다.")
        @Min(value = 2, message = "정원은 2명 이상이어야 합니다.")
        @Max(value = 100, message = "정원은 100명 이하여야 합니다.")
        Integer capacity,

        @Schema(description = "카카오 오픈채팅 초대 링크. `https://open.kakao.com/`으로 시작해야 한다. 승인된 파티원에게만 노출된다.",
                example = "https://open.kakao.com/o/abcd1234")
        @NotBlank(message = "카카오 오픈채팅 링크는 필수입니다.")
        @Pattern(regexp = "^https://open\\.kakao\\.com/.+$", message = "open.kakao.com 형식의 오픈채팅 링크만 등록할 수 있습니다.")
        String openChatUrl,

        @Schema(description = """
                모집 방식. `FIRST_COME`(선착순)은 참여 신청 시 정원이 남아있으면 즉시 파티원으로 확정된다.
                `APPROVAL`(승인제)은 참여 신청이 대기(PENDING) 상태로 들어가고 파티장이 수락해야 확정된다.
                생성 시에만 지정하며, 생성 후에는 변경할 수 없다(수정 요청에는 기존 값을 그대로 담아 보내면 된다).
                """, example = "APPROVAL")
        @NotNull(message = "모집 방식은 필수입니다.")
        StudyParty.RecruitmentType recruitmentType
) {
}
