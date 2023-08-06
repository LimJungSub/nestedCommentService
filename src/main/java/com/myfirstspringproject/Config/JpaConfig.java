package com.myfirstspringproject.Config;

import com.myfirstspringproject.Dto.UserAccountPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class JpaConfig {

    // todo(V) auditing 성공하게 수정 - ~By 오디팅 정보를 넣어주기 위함
    @Bean
    public AuditorAware<String> auditorAware() {
        return ()->
                Optional.ofNullable(SecurityContextHolder.getContext())
                        .map(SecurityContext::getAuthentication)
                        .filter(Authentication::isAuthenticated)
                        .map(Authentication::getPrincipal)
                        .map(UserAccountPrincipal.class::cast)
                        .map(UserAccountPrincipal::getUsername);
    }
}
