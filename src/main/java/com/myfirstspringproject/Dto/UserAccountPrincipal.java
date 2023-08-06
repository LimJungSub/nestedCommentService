package com.myfirstspringproject.Dto;

import com.myfirstspringproject.Domain.UserAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

//필요 시 record로 변경해도 되긴 함
public class UserAccountPrincipal implements UserDetails {

    //시큐리티를 활용하는데 adminMemo가 필요할까? 이용제한 등 조치필요하면 그럴수있긴하지.
    private String userId;
    private String userPassword;
    private String nickName;
    private String adminMemo;

    public UserAccountPrincipal(String userId, String userPassword, String nickName, String adminMemo) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.nickName = nickName;
        this.adminMemo = adminMemo;
    }

    public static UserAccountPrincipal deriveFromEntity(UserAccount userAccount){
        return new UserAccountPrincipal(
                userAccount.getUserId(),
                userAccount.getUserPassword(),
                userAccount.getNickname(),
                userAccount.getAdminMemo()
        );
    }

    public static UserAccountPrincipal of(
            String userId, String userPassword, String nickName, String adminMemo)
    {
        return new UserAccountPrincipal(userId, userPassword, nickName, adminMemo);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return userPassword;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum Position{
        USER("justUser");
        
        @Getter
        String position;

        Position(String p) {
            this.position = p;
        }
    }
}
