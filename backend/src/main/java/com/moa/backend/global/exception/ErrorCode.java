package com.moa.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공부파티입니다."),
    NOT_PARTY_OWNER(HttpStatus.FORBIDDEN, "파티장만 수행할 수 있는 작업입니다."),
    NOT_PARTY_MEMBER(HttpStatus.FORBIDDEN, "파티에 참여한 사용자만 볼 수 있습니다."),
    PARTY_FULL(HttpStatus.CONFLICT, "정원이 가득 찬 파티입니다."),
    PARTY_CLOSED(HttpStatus.CONFLICT, "모집이 마감된 파티입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여했거나 신청한 파티입니다."),
    NOT_JOINED(HttpStatus.CONFLICT, "참여하지 않은 파티입니다."),
    JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 이미 처리된 참여 신청입니다."),
    OWNER_CANNOT_LEAVE(HttpStatus.CONFLICT, "파티장은 파티를 탈퇴할 수 없습니다. 파티를 삭제해주세요."),
    INVALID_OPEN_CHAT_URL(HttpStatus.BAD_REQUEST, "카카오 오픈채팅 링크 형식이 올바르지 않습니다. (open.kakao.com)"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다. 다시 로그인해주세요."),
    KAKAO_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "카카오 인증에 실패했습니다."),
    DEV_LOGIN_DISABLED(HttpStatus.FORBIDDEN, "개발용 로그인은 로컬 환경에서만 사용할 수 있습니다."),
    PROFILE_INCOMPLETE(HttpStatus.FORBIDDEN, "회원가입을 완료해주세요. (반/조/이름 등록 필요)"),

    DINNER_PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 저녁팟입니다."),
    NOT_DINNER_PARTY_OWNER(HttpStatus.FORBIDDEN, "파티장만 수행할 수 있는 작업입니다."),
    DINNER_PARTY_FULL(HttpStatus.CONFLICT, "정원이 가득 찬 저녁팟입니다."),
    DINNER_PARTY_CLOSED(HttpStatus.CONFLICT, "모집이 마감된 저녁팟입니다."),
    DINNER_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여했거나 신청한 저녁팟입니다."),
    DINNER_NOT_JOINED(HttpStatus.CONFLICT, "참여하지 않은 저녁팟입니다."),
    DINNER_JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 이미 처리된 참여 신청입니다."),
    DINNER_OWNER_CANNOT_LEAVE(HttpStatus.CONFLICT, "파티장은 저녁팟을 탈퇴할 수 없습니다. 저녁팟을 삭제해주세요."),

    WEEKEND_PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주말팟입니다."),
    NOT_WEEKEND_PARTY_OWNER(HttpStatus.FORBIDDEN, "파티장만 수행할 수 있는 작업입니다."),
    WEEKEND_PARTY_FULL(HttpStatus.CONFLICT, "정원이 가득 찬 주말팟입니다."),
    WEEKEND_PARTY_CLOSED(HttpStatus.CONFLICT, "모집이 마감된 주말팟입니다."),
    WEEKEND_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여했거나 신청한 주말팟입니다."),
    WEEKEND_NOT_JOINED(HttpStatus.CONFLICT, "참여하지 않은 주말팟입니다."),
    WEEKEND_JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 이미 처리된 참여 신청입니다."),
    WEEKEND_OWNER_CANNOT_LEAVE(HttpStatus.CONFLICT, "파티장은 주말팟을 탈퇴할 수 없습니다. 주말팟을 삭제해주세요.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
