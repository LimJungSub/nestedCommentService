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
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "nestedcomment") //디비 스키마 내 테이블 이름 : nestedcommentservice
@NoArgsConstructor
public class Comment extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    //@Column(nullable = false) 부모댓글인 경우 nullable가능하므로 수정
    //자식 댓글의 경우 필수로 가져야하는 값이지만 프론트쪽에서 처리하므로써 null 방지 //내가 @Column 속성을 뺴버려서 그랬나.
    @Column(nullable = true)
    private Boolean isAffected;

    //jpa에서 fk를 나타낼떈 이 어노테이션 사용?
    @ManyToOne @JoinColumn(name="userId")
    private UserAccount user;
    //로그인은 안해도되는데... 이렇다면 createdBy, modifiedBy는 어떻게 설정해야할까?

    //자기 자신 참조 //여기가 Long이 아니라 Comment 였으면 뭐라고? 양방향매핑 (강의참고)
    @Column(nullable = true)
    private Long parentId;

    @Column(nullable = true)
    private String parentComment_Writer;

//    대댓글리스트, 댓글하나당 여러 대댓글 있으니 대댓글입장에서 1ㄷ1 매칭? ㄴㄴ
//    컬렉션을 디비는 나타낼 수 없으니 컬렉션은 '다'의 관점으로 보자, 외래키는? 부모댓글id. 주인은 comment
//    양방향관계던 단방향관계던 당연히 CASCADE 설정되겠지?, cascade는 허용되지 않음으로 설정
    @ToString.Exclude
    @Column(nullable = true)
    @OrderBy("createdDate asc")
    @OneToMany(mappedBy="parentId") //, cascade = CascadeType.DETACH
    private Set<Comment> childComments = new LinkedHashSet<>(); //순서유지?

//    @Embedded
//    public AuditingFields auditingFields;

    public Comment(String content, Boolean isAffected, UserAccount user, Long parentId, String parentComment_Writer) {
        this.content = content;
        this.isAffected = isAffected;
        this.user = user;
        this.parentId = parentId;
        this.parentComment_Writer = parentComment_Writer;
    }

    //서비스에서 saveComment라는 API를 공통으로 사용하다 보니 여기서 이렇게 Optional떡칠을 하게됐다... API분리 하는 방안도 고려
    public static Comment of(String content, Optional<Boolean> isAffected, UserAccount user, Optional<Long> parentId, String parentComment_Writer) {
        if(parentId.isPresent() && !isAffected.isPresent()) {
            return new Comment(content, null, user, parentId.get(), parentComment_Writer);
        }
        else if(!parentId.isPresent() && !isAffected.isPresent()){
            return new Comment(content, null, user, null, null);
        }
        else if(parentId.isPresent() && isAffected.isPresent()){
            return new Comment(content, isAffected.get(), user, parentId.get(), parentComment_Writer);
        }
        else if(!parentId.isPresent() && isAffected.isPresent()){
            return new Comment(content, isAffected.get(), user, null, null);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) && Objects.equals(content, comment.content) && Objects.equals(isAffected, comment.isAffected) && Objects.equals(user, comment.user)&& Objects.equals(parentId, comment.parentId) && Objects.equals(childComments, comment.childComments);
        //  &&
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, isAffected, user);
        //위 같이 빠진요소들 나중에 다시 넣어주기
    }

    public void addChildComment(Comment childComment){
        this.childComments.add(childComment);
        childComment.setParentId(this.id);
    }

    //DTO말고 여기선언된 함수들은 서비스쪽에서 DTO로 변환과정 거치기 전 테스트할 데이터들 대상
    public static boolean hasParent(Comment comment){
        return comment.getParentId()==null?false:true;
    }

    public static  boolean hasChild(Comment comment){
        return comment.getChildComments().size()!=0;
    }
//    @Embeddable
//    @ToString
//    @Getter
//    //@Setter 필요X 자동생성 들어가잖아. 마찬가지로 외부접근도 허용할 필요가 없으니 필드들 private로 놓자 (혹시 몰라 protected로 둚) -> 근데 차피 Comment단에서 private로 처리하긴했는데.. 이너클래스 사용시 어쨰되는지 모르곘으니 나중에 공부
//    //AuditingFields는 엔티티로 설정을 해야할까? 아닐거 같은데. 엔티티로 설정하면 스키마가 따로 만들어지는거 아닌가?
//    //AuditingFields는 CommentDto에서 .getAuditingFields까지는 에러가 안나는데 .getCreatedBy 등이 접근불가에러가 나서 public으로 변경함.
//    @EntityListeners(AuditingEntityListener.class)
//    public class AuditingFields{
//        @CreatedDate
//        @Column(nullable = false, updatable = false) @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
//        public LocalDateTime createdDate;
//        //ldt vs ld 전자가 권장되는걸로 앎
//
//        @LastModifiedDate
//        @Column(nullable = false, updatable = false) @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
//        public LocalDateTime modifiedDate;
//
//        @CreatedBy
//        @Column(nullable = false, length = 100, updatable = false)
//        public String createdBy;
//
////        @LastModifiedBy
////        @Column(nullable = false, length = 100, updatable = false)
////        private String modifiedBy;
//        //생각해보니 필요없음 (이슈 발행완)
//    }

}

