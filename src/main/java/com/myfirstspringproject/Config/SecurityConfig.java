package com.myfirstspringproject.Config;

import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //어떤 사람에게 어떤 권한을 줄지 미리 정해놨어야한다.
        //기타 정적 리소스들은 로그인되지 않은 유저에 대해서도 접근 허용
        return httpSecurity.authorizeHttpRequests(auth -> auth.requestMatchers(String.valueOf(PathRequest.toStaticResources().atCommonLocations()))
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/comments", "/")  //mvcMatchers deprecated : 원노트에 기록함 //여기 적혀있는 경로는 컨트롤러 매핑경로인듯. /comments 매핑하는 컨트롤러 지정안한상태로 요청 넣으면 로그인페이지 뜸 (이외 경로로 잡히니까)
                .permitAll()
                .anyRequest().authenticated()
        )   //여기까진 알겠는데, 이후 왜 formLogin이 적힐까? authorizeHttpRequests()가 HttpSecurity를 리턴하는 것은 확인했음. HttpSecurity에 직접적으로 formLogin이 적힐수있는건거같은데.
            //아마 permitAll들 제외하고, 이외 남겨놓은 authenticated()대상인 애들에 대해서만 formLogin이 적용되는 것같다.
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/")) // vs logout(logout -> logout.logoutSuccessUrl("/comments")) 내가 어떻게 매핑하냐에 따라 다르긴 하겠지
                .build();   //?
    }
}
