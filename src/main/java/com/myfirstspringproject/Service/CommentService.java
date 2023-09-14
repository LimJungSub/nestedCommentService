package com.myfirstspringproject.Service;

import com.myfirstspringproject.Domain.Comment;
import com.myfirstspringproject.Domain.UserAccount;
import com.myfirstspringproject.Dto.CommentDto;
import com.myfirstspringproject.Dto.UserAccountPrincipal;
import com.myfirstspringproject.Repository.CommentRepository;
import com.myfirstspringproject.Repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    public Page<CommentDto> getRootComments(Pageable pageable) {
        //바로 entity로 내보내면 안되므로 dto로 변환하는 로직 필요 , 서비스에서 구현하자
        //page로 싸져있는데 안까도되나? 여기서 의미상 컬렉션으로 리턴될텐데... 음?
        //즉 부모만 뽑아오긴 했음commentRepository.findByParentIdIsNull() 결과:[AuditingFields(createdDate=2023-08-06T16:45:56.602796, modifiedDate=2023-08-06T16:45:56.602796, createdBy=user2), AuditingFields(...
        //Auditing쪽만 toString 되어있어서 위처럼 출력된 듯, 밀리언초를 아이디처럼 생각

        //해결 : 디티오에 전체댓글리스트를 넘겨서 organizeChildCOmments결과(전체리스트)를 받고 서비스(여기)에서는 해당 리스트에 대해 부모인것만 리턴해주면 계층구조도 잡힌 채 부모만 리턴 가능하지 않을까?
        //return commentRepository.findAll(pageable).map(CommentDto::toDto).stream().filter(CommentDto::hasParent); //이렇게 하면 페이저블로 리턴을 못하고 스트림으로 리턴이 된다.
//        return commentRepository.findByParentIdIsNull(pageable).
//                map(CommentDto::organizeChildsAndToDtoAndGetRootOnly); //처럼 맵만 하고 리턴해줘야 페이지가 사는거같다. 그러므로 toDto와 organizeChildComments, 그리고 부모 리스트만 리턴해주는 것을 한번에 하는 함수가 필요


//        Stream<CommentDto> commentDtoStream = allList.stream().map(CommentDto::toDto);
//        commentDtoStream.filter(CommentDto::hasParent).collect(Collectors.toList());
        //이렇게

//        버전3 들어가기 전, 정말로. 자자식데이터가 뷰쪽으로 넘어오는게 맞는지 확인하기 => 결론 : 잘 넘어옴
        List<Comment> list = commentRepository.findByParentIdIsNull();
        list.stream().forEach(
                c -> {
                    //루트댓글의 직계자식만 찍기
                    log.info("[자식출력]댓글번호 " + c.getId() + "의 c.getChildComments(): " + c.getChildComments());

                    //자식댓글 중 자식(자자식)이 있는애들만 걸러서 위와 같은 방식으로 childComments 찍기
                    c.getChildComments().stream().filter(Comment::hasChild).forEach(
                            c2 -> {
                                log.info("[자자식출력]댓글번호 " + c2.getId() + "의 c.getChildComments(): " + c2.getChildComments());
                            }
                    );
                }
        );

        log.info("commentRepository.findByParentIdIsNull() 결과:" + commentRepository.findByParentIdIsNull().toString());
        return commentRepository.findByParentIdIsNull(pageable).map(CommentDto::toDto);

//        return commentRepository.findAll(pageable).map(CommentDto::toDto);
        //Page에서 제공하는 map이라 .stream().filter(c->!c.hasParent(c)).가 불가능하다...

        //대댓글 기능 구현 후 수정 필요, 컨트롤러가 여기서 댓글들 갖고와서 뷰에다 뿌려주므로 이 부분에서 정렬구현 필요.
        //일차원적으로 데이터베이스에 펼쳐진 데이터를 구조적으로 편성하는 것이 중요
        //정렬은? 사실 세이브될떄 구현되어야하는 것이므로... 서비스레이어의 save쪽에서 구현해주면될듯?
        //현재도, 부모댓글(부모댓글없는놈들)들이 오름차순으로(등록순)대로 등록되고있다. 우리가 원하는 것은 부모댓글은 내림차순으로, 자식댓글은 오름차순(등록순)으로 구현되어야하므로 save쪽을 대대적으로 손봐야할듯, treeset을 어디다 구현해놓지? 디티오쪽일듯.

        //Page<CommentDto>를 반환하는데 어떻게 Set을 사용할 수 있을까? ... 디비자체에서 정렬을 해놓길원한다 vs 뿌려줄때 정렬되게 보이기만 해도된다. 후자면 Page 정렬설정하면 끝이다, 단지 속성으로 가지고있는 childComments만 정렬하는것이 문제가된다.
        //엔티티쪽에 Treeset으로 정의하려 했더니 both sorted error, 어떻게 해야할까? 근데 요청할때마다 정렬해서 주는거는 낭비가 심하지 않나. 한번 정렬시켜놓는게 낫지.
        //그냥 findall(pageable X) 해서 가져오고, 했던 로직그대로 적용
        //최종적으로는 1차에는 부모댓글만 있는 상태로 내보낸다.
        //부모아이디가 같은 댓글들 끼리 모아서 스트림 돌리기
//        List<CommentDto> comments = commentRepository.findAll().stream().map(CommentDto::toDto).collect(Collectors.toList());
//        Map<Long, CommentDto> map = comments.stream().collect(Collectors.toMap(CommentDto::id, Function.identity()));
//        map.values().

        //내 판단으로는 그냥 dto로만 변경후에 부모댓글만 담아서 리턴해주면 어차피 부모 속성으로 담겨있으니까 대댓글까지 접근할수있을것같음, Page로 캐스팅하는거 괜찮을려나. 안됨. 런타임에러남. 아래코드
        //return (Page<CommentDto>) commentRepository.findAll().stream().map(CommentDto::toDto).filter(dto->CommentDto.hasParent(dto)).collect(Collectors.toList());
    }

    //crud 작업에서도 Page<T>가 필요한가? 강의내용보니 필요없는것 같다!
    public void saveComment(String content, Optional<Boolean> isAffected, String userId, Optional<Long> parentCommentId) {
        //content와 principal 받음 -> commentdto(comment)로 변환하여 repository에 넘겨줘야
        //comment를 구성하기 : content, isaffected, userid, parentid, childcomments
        //이 중 parentId, childComments는 어떻게 완성해야할까? -> 아마 컨트롤러쪽에서 이쪽으로 미리 넘겨줬어야했을텐데... 미리 대댓글 구현 방법을 봐놔야 쉬울듯.
        //위 방식도 컨트롤러에서 방법이 바뀜에 따라 취소된 사항.
        UserAccount user = userAccountRepository.findByUserId(userId).get();

        //자식코멘트일 때, addChildComment 수행 및 ParentComment_Writer 세팅
        if (parentCommentId.isPresent()) {
            //부모댓글작성자를 가져온 후 세팅
            String parentComment_Writer = commentRepository.findById(parentCommentId.get()).get().getCreatedBy();
            log.info("(서비스) commentRepository.findById(parentCommentId.get()).get()"+commentRepository.findById(parentCommentId.get()).get());
//            log.info("(서비스) parentComment_Writer세팅"+ commentRepository.findById(parentCommentId.get()).get().getParentComment_Writer());
            Comment targetComment = Comment.of(content, isAffected, user, parentCommentId, parentComment_Writer);
            //부모엔티티를 가지고와서, 부모엔티티에 자식댓글로 등록해준다.
            Comment parentComment = commentRepository.getReferenceById(parentCommentId.get());
            parentComment.addChildComment(targetComment);
            log.info("parentComment.addChildComment(targetComment); 실행 후 " + parentComment.getId() + "의 자식: " + parentComment.getChildComments());
            commentRepository.save(targetComment);
        }
        //루트코멘트일때 실행
        else {
            Comment targetComment = Comment.of(content, isAffected, user, parentCommentId, null);
            commentRepository.save(targetComment);
        }
        //get했을떄 null이나 Long이 전달된다.
        //인자 일부만을 받아서 안됐던 것일까?
    }

    public void deleteComment(Long commentId) {
        //아이디로 객체를 가져와서 자식을 가져오고 자식set을 순회하며 해당엔티티의 삭제여부를 결정
        Comment targetComment = commentRepository.findById(commentId).get();
        targetComment.getChildComments().stream().forEach(comment -> {
                    if (comment.getIsAffected() == true) {
                        commentRepository.deleteById(comment.getId());
                    }
                }
        );
        commentRepository.deleteById(commentId);

    }

    public void updateComment(Long commentId, String content, Boolean isAffected) {
        //Dirty checking을 활용하여, 엔티티를 가져오고 수정한다.
        Comment comment = commentRepository.getReferenceById(commentId);
        if (comment.getContent() != null) {
            comment.setContent(content);
        }
        if (comment.getIsAffected() != null) {
            comment.setIsAffected(isAffected);
        }
    }


    //GET이므로 우선 단순하게 Entity그대로 받아오자.
    public List<Comment> getSomeCommentsEntity() {
        List<Comment> comments = List.of(
                //자식댓글
                commentRepository.findById(1802L).get()
                //루트댓글
                //commentRepository.findById(1702L).get()
        );
        return comments;
    }
}
