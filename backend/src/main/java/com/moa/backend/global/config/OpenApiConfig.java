package com.moa.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI(springdoc-openapi) 문서 설정.
 *
 * <p>접속: 로컬 실행 시 {@code http://localhost:8080/swagger-ui.html}
 * <p>Raw 스펙(JSON): {@code http://localhost:8080/v3/api-docs}
 *
 * <p>대부분의 API는 {@code Authorization: Bearer {accessToken}} 헤더가 필요하다.
 * Swagger UI 우측 상단의 "Authorize" 버튼을 눌러 로그인/카카오 로그인/개발용 로그인으로 발급받은
 * {@code accessToken} 값을 넣으면, 이후 "Try it out" 요청에 자동으로 헤더가 실린다.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SECURITY_SCHEME, bearerAuthScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("모공(MoaStudy) API")
                .description("""
                        '모아봐요 공부팀!' 스터디 파티 매칭 서비스의 REST API 문서입니다.

                        ## 인증 방식
                        - 카카오 로그인(`POST /api/auth/kakao`) 또는 로컬 개발용 로그인(`POST /api/auth/dev-login`)으로
                          `accessToken` / `refreshToken`을 발급받습니다.
                        - 이후 모든 인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.
                        - `accessToken`은 30분(기본값)으로 짧게 만료되며, 만료 시 `POST /api/auth/refresh`에
                          `refreshToken`을 보내 재발급받습니다.
                        - 이 화면 우측 상단의 **Authorize** 버튼에 `accessToken` 값만 넣으면 이후 모든 요청에
                          자동으로 헤더가 실립니다 (`Bearer` 접두사는 자동으로 붙습니다).

                        ## 가입(온보딩) 흐름
                        카카오 로그인이 성공해도 바로 서비스를 이용할 수 있는 것은 아닙니다. 처음 로그인한
                        사용자는 `profileCompleted = false` 상태로 생성되며, 반(1~10) / 조 / 실명을
                        `PATCH /api/users/me/onboarding`으로 제출해야 `profileCompleted = true`로 바뀌면서
                        가입이 완료됩니다. 가입을 완료하지 않으면 파티 생성/참여 신청이 `403 PROFILE_INCOMPLETE`로
                        거부됩니다.

                        ## 파티 참여 승인 흐름
                        파티 참여는 신청 즉시 멤버가 되는 구조가 아닙니다.
                        1. `POST /api/parties/{id}/join` → `PENDING`(대기) 상태의 참여 신청 생성
                        2. 파티장이 `GET /api/parties/{id}/join-requests`로 대기 목록을 확인
                        3. `POST /api/parties/{id}/join-requests/{memberId}/approve`(수락) 또는
                           `.../reject`(거절)로 처리
                        4. 정원 초과 여부는 신청 시점이 아니라 **수락 시점**에만 검사합니다.

                        오픈채팅 링크(`openChatUrl`)는 파티장 본인이거나 `memberStatus = APPROVED`인
                        사용자에게만 응답에 포함됩니다.
                        """)
                .version("v1.0.0")
                .contact(new Contact().name("MoaStudy(모공)").email("support@moastudy.example.com"));
    }

    private SecurityScheme bearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("로그인으로 발급받은 accessToken. 값만 입력하면 'Bearer ' 접두사는 자동으로 붙습니다.");
    }
}
