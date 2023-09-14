package com.myfirstspringproject.Dto;

import com.myfirstspringproject.Domain.Comment;
import com.myfirstspringproject.Domain.UserAccount;
import com.myfirstspringproject.Repository.UserAccountRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

//정말 내부적으로만 필요한 것 아니면 웬만하면 dto에는 엔티티와 일치하게 설계? ... 음 물론 어디계층에서 어떻게쓰느냐가 중요하긴함.
//나는 컨트롤러에서는 dto를 사용을 안할 것임. 즉 서비스에서 돌려줄떄 dto로 돌려준다는 뜻.
//얘는 단일 Comment를 다루는 Dto이다.
public record CommentDto(
        Long id,
        String content,
        Boolean isAffected,
        String userId,
        String userNickname,
        Long parentId,
        String parentComment_Writer,
//        Set<Comment> childComments, //index.html 대댓글구현 부분 참고, 왜 Set<CommentDto>로 바꾸는지
        Set<CommentDto> childComments,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String createdBy
) {
    //Long parentId, Set<Comment> childComments,
    public static CommentDto of(Long id, String content, Boolean isAffected, String userId,  String userNickname, Long parentId, String parentComment_Writer,
                                Set<CommentDto> childComments, LocalDateTime createdDate, LocalDateTime modifiedDate, String createdBy) {
        //위 Set<CommentDto> childComments를 Set<Comment> childComments로 변경... dto에서 dto를 받는건 뭐여...
        return new CommentDto(id,content,isAffected,userId, userNickname, parentId, parentComment_Writer, childComments, createdDate,modifiedDate,createdBy);
    }


//    //서비스에서 ::toDto로 변환과정을 거쳤는데... 이젠 그럴필요가 있을까, 그냥 완성된 계층구조 잡힌 댓글리스트 하나만 내려주면 된다. 그 중 Parent만 골라서... 계층구조 까지 다 잡힌 Set 중 루트만 내려주는 것으로 구현하면 뷰에서 돌때도 문제는 없을것같다.
      //즉 서비스쪽에서, 구조가 다잡힌 댓글엔티티를 받고, 부모만 컨트롤러쪽으로 내려준다는 것이다. 이러면, N차대댓글 구조까지 다 잡히게 되었을까?
      //아래 toDto는 루트댓글이 적용대상이다.
//    public static Set<CommentDto> getCommentDtoStructure(Set<CommentDto> allComments){
//
//    }

    //부모댓글들만 toDto 적용 -  comment.getChildComments() / 즉 자식댓글들만 Set<CommentDto> set으로 설정 - 자식댓글의 자식댓글들은? 설정이안됨 - 그래서 옛날에 전체코멘트리스트에 대해서 toDto를 할때는 자자식들까지 다 잘바뀌었던 것 같다.
    //엔티티로 부터 dto
    public static CommentDto toDto(Comment comment){
        //대댓글정렬
        Comparator<CommentDto> comparator = Comparator.comparing(CommentDto::createdDate).thenComparingLong(CommentDto::id);

        //직계자식을 담은 Set 설정
        Set<CommentDto> childSet = comment.getChildComments().stream().map(
                        c-> {   //c is childComment(자식댓글) ... 자식댓글의 Set을 Null로 뒀으니 에러가 났던 것 같다.
                            //CommentDto::of로직 그대로 사용하면 됨, 대신 Set<CommentDto>만 null인 상태로. -> 시발 여기가 문제였을듯.
                            //이 부분에서 자식코멘트가 있다면 자식코멘트를 childComments로 지정해주는 행동이 필요하다
                            //c(자식)에 대해서 자식이있다면 자식으로 추가해준다. null로 둘게 아니다.

                            //대댓글의 댓글(N차대댓글)들도 대댓글과 정렬로직이 똑같기떄문에(등록순), 똑같은 comparator 적용
                            Set<CommentDto> tmpSet = c.getChildComments().stream().map(CommentDto::toDto).collect(Collectors.toSet());
                            TreeSet<CommentDto> childTreeSet = new TreeSet<>(comparator);
                            childTreeSet.addAll(tmpSet);
                            return CommentDto.of(
                                    c.getId(), c.getContent(), c.getIsAffected(), c.getUser().getUserId(), c.getUser().getNickname(), c.getParentId(), c.getParentComment_Writer(),
                                    //자식코멘트의 자식코멘트들도 대댓글에 속하므로 대댓글과 같은 정렬로직을 적용
                                    childTreeSet,
                                    c.getCreatedDate(), c.getModifiedDate(), c.getCreatedBy()
                            );
                        }).collect(Collectors.toSet());  //Set<CommentDto>
        TreeSet<CommentDto> treeSet = new TreeSet<>(comparator);   //초기값셋과 정렬기준(컴페레이터)를 한꺼번에 넘겨주고 싶으나 그런메서드는 없으므로 우선 정렬기준 설정 후 addAll 사용
        treeSet.addAll(childSet);

        return new CommentDto(
                //comment.getUserId().getUserId() 불편 ... -> 엔티티에서 userId가 UserAccount로 설정되어있었네... 어떻게 리팩토링할까? 이대로써도되긴함.
                comment.getId(), comment.getContent(), comment.getIsAffected(), comment.getUser().getUserId(), comment.getUser().getNickname() , comment.getParentId(), comment.getParentComment_Writer(),
                //Required type:
                //Set~
                //<CommentDto>
                //Provided:
                //Set
                //<Comment> 이므로 comment.getChildComments()만 해서는 안된다. Set<Comment> -> Set<CommentDto>
                //★이쪽에서 자식댓글의 정렬을 구현해주면...
                treeSet
                //음 엔티티에서도 Set<CommentDto>로 들고있는게 낫나? 아닐거야 전혀. 엔티티는 리포지토리니까.그렇다면
                , comment.getCreatedDate(), comment.getModifiedDate(),
                comment.getCreatedBy()
        );
    }

    //루트댓글인지 확인할 때 쓰는 메서드
    public Boolean hasParent(){
        return this.parentId != null ? true : false;
    }

    //자식댓글을 갖고있는지 확인할 때 쓰는 메서드
    public Boolean hasNotChildComments(){
        return this.childComments.isEmpty();
    }


//    어라.. orgainzed안해도 n차대댓글이 출력되고 디비에도 잘 설정됨. 고로 안해도될듯, index.html-라벨((1)..들어있는거참고)
//    public static CommentDto  organizeChildsAndToDtoAndGetRootOnly(Set<Comment> comments) {
//        //organizeChilds
//        comment.get
//    }


    //디티오로부터 엔티티, 디티오가있은 후 엔티티로 변하니 스태틱메소드 X
//    public Comment toEntity(String content, Boolean isAffected, UserAccount user){
//        //id, auditingfield는 여기서 안받아도 될거고
//        //userId, content, isAffected는 여기서 받아야됨.
//        //childComments, parentId는 나중에 세팅하는 과정 거치면 될 듯?
//
//        //UserAccount findByUserId
//
//        //엔티티에 위 인자들을 그대로 받는 생성자 및 팩토리메소드of 생성
//        return Comment.of();
//
//    }
}
