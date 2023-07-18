package com.myfirstspringproject.Config;

import com.myfirstspringproject.Dto.UserAccountPrincipal;
import com.myfirstspringproject.Repository.userAccountRepository;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                .build();   //todo: 로그아웃은 나중에 로그아웃 alert창띄워서 확인하는 방법으로 변경할 것. 지금은 스프링 기본제공 로그아웃확인창 사용 중.
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 유저정볼르 찾기위해 repository에서 findBy를 통해 가지고 오는 기본적인 원리
    @Bean
    public UserDetailsService userDetailsService(userAccountRepository repository){
        //람다식으로 이렇게 바로 리턴할수있는 이유? 익명함수같은거랑 관련있을거임
        //findById 옵셔널로 들어있으니 까주고, 아 까주는건 마지막에 해야하나봐!, map 인자 자체가 옵셔널이 널이아닐떄만 실행되는거네.
        //findById는 파라미터로 Long을 받는다. 근데 userId가 Long이 아니라 String인 상황. 그냥 하나 커스텀해서 만드는게 쉽긴한데, 정말 findById는 Long만 받을수있나
        return username -> repository.findByUserId(username)
                .map(UserAccountPrincipal::deriveFromEntity)
                .orElseThrow(()->new UsernameNotFoundException("유저정보검색 Error"));
    }
}
