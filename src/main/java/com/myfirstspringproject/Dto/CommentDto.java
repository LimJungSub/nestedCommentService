package com.myfirstspringproject.Dto;

import com.myfirstspringproject.Domain.Comment;

import java.time.LocalDateTime;
import java.util.Set;

//정말 내부적으로만 필요한 것 아니면 웬만하면 dto에는 엔티티와 일치하게 설계? ... 음 물론 어디계층에서 어떻게쓰느냐가 중요하긴함.
//나는 컨트롤러에서는 dto를 사용을 안할 것임. 즉 서비스에서 돌려줄떄 dto로 돌려준다는 뜻.
public record CommentDto(
        Long id,
        String content,
        Boolean isAffected,
        String userId,
        Long parentId,
        Set<Comment> childComments,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String createdBy
) {
    public static CommentDto of(Long id, String content, Boolean isAffected, String userId, Long parentId, Set<Comment> childComments, LocalDateTime createdDate, LocalDateTime modifiedDate, String createdBy) {
        return new CommentDto(id,content,isAffected,userId,parentId,childComments,createdDate,modifiedDate,createdBy);
    }
    
    //엔티티로 부터 dto
    public static CommentDto toDto(Comment comment){
        return new CommentDto(
                //comment.getUserId().getUserId() 불편 ... -> 엔티티에서 userId가 UserAccount로 설정되어있었네... 어떻게 리팩토링할까? 이대로써도되긴함.
                comment.getId(), comment.getContent(), comment.getIsAffected(), comment.getUserId().getUserId(), comment.getParentId(),
                comment.getChildComments(), comment.getAuditingFields().getCreatedDate(), comment.getAuditingFields().getModifiedDate(),
                comment.getAuditingFields().getCreatedBy()
        );
    }

}
