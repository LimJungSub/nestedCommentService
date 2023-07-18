package com.myfirstspringproject.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Table(name = "useraccount")
@Entity
//@NoArgsConstructor 지웠는데도 왜 에러가 발생을 안할까?
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String userId;

    //jpa에서 not null을 표현하는 방법?
    @Column(nullable = false)
    private String userPassword;

    private String nickname;

    private String adminMemo;
}

//단방향 매핑을 사용한다. 유저어카운트에서 굳이 특정유저가 작성한 댓글목록을 두지 않을 것이기 때문

