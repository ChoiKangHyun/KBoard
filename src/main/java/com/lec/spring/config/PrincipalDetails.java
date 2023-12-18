package com.lec.spring.config;

import com.lec.spring.domain.Authority;
import com.lec.spring.domain.User;
import com.lec.spring.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class PrincipalDetails implements UserDetails, OAuth2User {
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private UserService userService;
    private User user;

    public User getUser() {
        return user;
    }

    // 일반 로그인 용 생성자
    public PrincipalDetails(User user){
        System.out.println("UserDetails(user) 생성: " + user);
        this.user = user;
    }

    // OAuth2 로그인용 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;    // 이 때 User 정보는, 인증 직후 provider로 부터 받은 attributes 를 토대로 생성
        this.attributes = attributes;
    }

    // 해당 User 의 '권한(들)'을 리턴
    // 현재 로그인한 사용자의 권한정보가 필요할때마다 호출된다. 혹은 필요할때마다 직접 호출해 사용할수도 있다
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("getAuthorities() 호출");

        Collection<GrantedAuthority> collect = new ArrayList<>();


        List<Authority> list = userService.selectAuthoritiesById(user.getId());
        for (Authority auth : list) {
            collect.add(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return auth.getName();
                }

                //thymeleaf 등에서 확인 활용하기 위한 문자열(학습용)
                @Override
                public String toString() {
                    return auth.getName();

                }
            });
        }

        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // 계정이 만료되지 않았는지
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //계정이 잠긴건 아닌지
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 계정 credential이 만료된건 아닌지
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화 되어있는지
    @Override
    public boolean isEnabled() {
        return true;
    }

    //----------------------------------------
    //OAuth2User 를 implementation하면 구현할 메소드

    private Map<String ,Object> attributes; // <- OAuth2User 의 getAttributes() 값



    @Override
    public String getName() {
        return null;  // 사용하지 않을 예정
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes; // 생성자에서 받아온다.
    }
}
