package com.example.backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReturnCode {

    // 상태코드 위키피디아 : https://ko.wikipedia.org/wiki/HTTP_%EC%83%81%ED%83%9C_%EC%BD%94%EB%93%9C
    USER_EXIST_USING_THIS_EMAIL(400, "해당 이메일로 이미 가입한 계정이 존재합니다."),
    FAIL_TO_GET_KAKAO_ACCOUNT(500, "카카오 계정 정보를 불러오는데에 실패하였습니다."),
    FAIL_TO_GET_KAKAO_ACCESS_TOKEN_INFO(500, "카카오 토큰 정보 보기 실패"),
    FORGED_EMAIL(400,"이메일 검증 실패"),
    SUCCESS(200,"성공"),
    USER_NOT_FOUND(404,"해당 요청 정보의 사용자가 존재하지 않습니다.");

    private final int status;
    private final String message;
}
