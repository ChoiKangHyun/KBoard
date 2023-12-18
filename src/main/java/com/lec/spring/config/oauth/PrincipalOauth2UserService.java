package com.lec.spring.config.oauth;

import com.lec.spring.config.PrincipalDetails;
import com.lec.spring.config.oauth.provider.FacebookUserInfo;
import com.lec.spring.config.oauth.provider.NaverUserInfo;
import com.lec.spring.config.oauth.provider.OAuth2UserInfo;
import com.lec.spring.config.oauth.provider.GoogleUserInfo;
import com.lec.spring.domain.User;
import com.lec.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2UserService<OAuth2UserRequest, OAuth2User>(I)
 * └─ DefaultOAuth2UserService
 */

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    // 여기서 인증 후 '후처리'
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.oauth2.password}")
    private String oauth2Password;

    //인증 직후 loadUser() 는 provider 로 부터 받은 userRequest 데이터에 대한 후처리 진행
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("""
                loadUser() 호출
                ClientRegistration: %s
                RegistrationId: %s
                AccessToken: %s
                OAuth2User Attributes: %s                
                """.formatted(userRequest.getClientRegistration(),  //ClientRegistration
                userRequest.getClientRegistration().getRegistrationId(), //String
                userRequest.getAccessToken().getTokenValue(),  //String
                oAuth2User.getAttributes())  // Map<String, Object> <- 사용자 프로필 정보가 담겨 있음
        );

        // 후처리 : 회원가입 진행
        String provider=userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo= switch (provider.toLowerCase()){

            case "google" -> new GoogleUserInfo(oAuth2User.getAttributes());
            case "facebook"-> new FacebookUserInfo(oAuth2User.getAttributes());
            case "naver"-> new NaverUserInfo(oAuth2User.getAttributes());
            default->null;
        };
        String providerId = oAuth2UserInfo.getProviderId();
        // username은 중복되지 않도록 만들어야 한다
        String username = provider + "_" + providerId; // "ex) google_xxxxxxxx"
        String password = passwordEncoder.encode(oauth2Password);
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        // 회원 가입 진행하기 전에
        // 이미 가입한 회원인지, 혹은 비가입자인지 체크하여야 한다
        User newUser = User.builder()
                .username(username)
                .name(name)
                .email(email)
                .password(password)
                .provider(provider)
                .providerId(providerId)
                .build();

        User user = userService.findByUsername(username);
        if (user == null) {  // 비가입자인 경우에만 회원 가입 진행
            user = newUser;
            int cnt = userService.register(user);  // 회원 가입!
            if (cnt > 0) {
                System.out.println("[OAuth2 인증 회원가입 성공]");
                user = userService.findByUsername(username);
            } else {
                System.out.println("[OAuth2 인증 회원 가입 실패");
            }
        } else {
            System.out.println("[OAuth2 인증. 이미 가입된 회원입니다");
        }

        PrincipalDetails principalDetails = new PrincipalDetails(user, oAuth2User.getAttributes());
        principalDetails.setUserService(userService);

        return principalDetails;
    }

}
