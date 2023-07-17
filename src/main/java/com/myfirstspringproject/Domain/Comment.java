package com.myfirstspringproject.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.annotation.Order;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "nestedcomment") //디비 스키마 내 테이블 이름 : nestedcommentservice
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean isAffected;

    //jpa에서 fk를 나타낼떈 이 어노테이션 사용?
    @ManyToOne @JoinColumn(name="userId")
    private UserAccount userId;
    //로그인은 안해도되는데... 이렇다면 createdBy, modifiedBy는 어떻게 설정해야할까?

    //자기 자신 참조 //여기가 Long이 아니라 Comment 였으면 뭐라고? 양방향매핑 (강의참고)
    @Column
    private Long parentId;

    //대댓글리스트, 댓글하나당 여러 대댓글 있으니 대댓글입장에서 1ㄷ1 매칭? ㄴㄴ
    //컬렉션을 디비는 나타낼 수 없으니 컬렉션은 '다'의 관점으로 보자, 외래키는? 부모댓글id. 주인은 comment
    //양방향관계던 단방향관계던 당연히 CASCADE 설정 되겠지?, cascade는 허용되지 않음으로 설정
    @ToString.Exclude
    @OrderBy("auditingFields.createdDate asc")
    @OneToMany(mappedBy="parentId", cascade = CascadeType.PERSIST) //, cascade = CascadeType.DETACH
    private Set<Comment> childComments;

    @Embedded
    private AuditingFields auditingFields;

    @Embeddable
    @Getter
    //@Setter 필요X 자동생성 들어가잖아. 마찬가지로 외부접근도 허용할 필요가 없으니 필드들 private로 놓자 (혹시 몰라 protected로 둚) -> 근데 차피 Comment단에서 private로 처리하긴했는데.. 이너클래스 사용시 어쨰되는지 모르곘으니 나중에 공부
    //AuditingFields는 엔티티로 설정을 해야할까? 아닐거 같은데. 엔티티로 설정하면 스키마가 따로 만들어지는거 아닌가?
    @EntityListeners(AuditingEntityListener.class)
    protected class AuditingFields{
        @CreatedDate
        @Column(nullable = false, updatable = false) @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime createdDate;
        //ldt vs ld 전자가 권장되는걸로 앎

        @LastModifiedDate
        @Column(nullable = false, updatable = false)
        private LocalDateTime modifiedDate;

        @CreatedBy
        @Column(nullable = false, length = 100, updatable = false)
        private String createdBy;

//        @LastModifiedBy
//        @Column(nullable = false, length = 100, updatable = false)
//        private String modifiedBy;
        //생각해보니 필요없음 (이슈 발행완)
    }

}

