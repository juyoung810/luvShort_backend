package com.example.backend.controller;

import com.example.backend.config.auth.dto.KakaoAccessToken;
import com.example.backend.config.auth.dto.KakaoUserInfoResponseDto;
import com.example.backend.domain.user.User;
import com.example.backend.dto.ResponseDTO;
import com.example.backend.dto.SignInDto;
import com.example.backend.exception.BackendException;
import com.example.backend.jwt.TokenProvider;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import com.example.backend.service.api.KakaoApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final KakaoApiService kakaoApiService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    // step3: 카카오 서버에서 받은 토큰을 다시 보내서 사용자 정보 조회
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody KakaoAccessToken requestDto) throws BackendException {
        log.info("KakaoAccessToken: {}",requestDto);
        String accessToken = requestDto.getAccess_token();
        // step3 사용자 정보 가져오기
        KakaoUserInfoResponseDto dto = kakaoApiService.getKakaoAccount(accessToken);
        log.info("id : {}", dto.getId());
        log.info("has_signed_up : {}", dto.getHas_signed_up());
        log.info("connected_at : {}", dto.getConnected_at());
        log.info("synched_at : {}", dto.getSynched_at());
        log.info("properties : {}", dto.getProperties());
        log.info("KakaoAccount : {}", dto.getKakaoAccount());

        /**
         * HTTP/1.1 200 OK
         * {
         *     "id":123456789,
         *     "kakao_account": {
         *         "profile_needs_agreement": false,
         *         "profile": {
         *             "nickname": "홍길동",
         *             "thumbnail_image_url": "http://yyy.kakao.com/.../img_110x110.jpg",
         *             "profile_image_url": "http://yyy.kakao.com/dn/.../img_640x640.jpg",
         *             "is_default_image":false
         *         },
         *         "name_needs_agreement":false,
         *         "name":"홍길동",
         *         "email_needs_agreement":false,
         *         "is_email_valid": true,
         *         "is_email_verified": true,
         *         "email": "sample@sample.com",
         *         "age_range_needs_agreement":false,
         *         "age_range":"20~29",
         *         "birthday_needs_agreement":false,
         *         "birthday":"1130",
         *         "gender_needs_agreement":false,
         *         "gender":"female"
         *     },
         *     "properties":{
         *         "nickname":"홍길동카톡",
         *         "thumbnail_image":"http://xxx.kakao.co.kr/.../aaa.jpg",
         *         "profile_image":"http://xxx.kakao.co.kr/.../bbb.jpg",
         *         "custom_field1":"23",
         *         "custom_field2":"여"
         *         ...
         *     }
         * }
         */

        return new ResponseEntity<>("{}", HttpStatus.OK);

    }

    // NOTE: JWT 기반 로그인은 모든 요청마다 사용자를 검증하는 방식으로 구현함 (세션과의 차이점)
    //  Spring Security가 모든 요청마다 서블릿 필터같은 역할을 해서 사용자 검증함
    //  로그인할 때 사용자 검증에 쓸 token과 SignInDto 생성
    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody SignInDto signInDto) {
        Optional<User> user = userRepository.findByEmail(signInDto.getEmail());

        if(user.isPresent()) {
            final String token = tokenProvider.createJws(user.get()); // 로그인 할 때 토큰 생성
            final SignInDto responseSignInDto = SignInDto.builder()
                    .token(token)
                    .email(signInDto.getEmail())
                    .build();
            return ResponseEntity.ok().body(responseSignInDto);
        } else {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Login failed.")
                    .build();
            return ResponseEntity
                    .badRequest()
                    .body(responseDTO);
        }
    }



}
