package com.myfirstspringproject.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String content;

    private Boolean isAffected;

    //로그인은 안해도되는데... 이렇다면 createdBy, modifiedBy는 어떻게 설정해야할까?

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

        @LastModifiedBy
        @Column(nullable = false, length = 100, updatable = false)
        private String modifiedBy;
    }

}

