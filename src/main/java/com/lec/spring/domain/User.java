package com.lec.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;

    @JsonIgnore
    private String password;

    @ToString.Exclude  // toString() 결과에서 제외
    @JsonIgnore
    private String re_password; // 비밀번호 확인 입력


    private String name;
    private String email;

    @JsonIgnore
    private LocalDateTime regDate;

    // OAuth2
    private String provider; // 어떤 OAuth2 제공자 kakao, Naver, Google..
    private String providerId; // provider 의 고유 id 값


}
