package com.myfirstspringproject.Service;

import com.myfirstspringproject.Domain.Comment;
import com.myfirstspringproject.Domain.UserAccount;
import com.myfirstspringproject.Dto.CommentDto;
import com.myfirstspringproject.Dto.UserAccountPrincipal;
import com.myfirstspringproject.Repository.CommentRepository;
import com.myfirstspringproject.Repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    public Page<CommentDto> getComments(Pageable pageable){
        //바로 entity로 내보내면 안되므로 dto로 변환하는 로직 필요 , 서비스에서 구현하자.
        return commentRepository.findAll(pageable) //page로 싸져있는데 안까도되나? 여기서 의미상 컬렉션으로 리턴될텐데... 음?
                .map(CommentDto::toDto);
    }
    //crud 작업에서도 Page<T>가 필요한가? 강의내용보니 필요없는것 같다!
    public void saveComment(String content, Boolean isAffected, String userId){
        //content와 principal 받음 -> commentdto(comment)로 변환하여 repository에 넘겨줘야
        //comment를 구성하기 : content, isaffected, userid, parentid, childcomments
        //이 중 parentId, childComments는 어떻게 완성해야할까? -> 아마 컨트롤러쪽에서 이쪽으로 미리 넘겨줬어야했을텐데... 미리 대댓글 구현 방법을 봐놔야 쉬울듯.
        //위 방식도 컨트롤러에서 방법이 바뀜에 따라 취소된 사항.
        UserAccount user = userAccountRepository.findByUserId(userId).get();

        //Comment 엔티티만들기 : of()
        commentRepository.save(Comment.of(content, isAffected, user));
        //인자 일부만을 받아서 안됐던 것일까?
    }
    
    public void deleteComment(Long commentId){
        commentRepository.deleteById(commentId);
    }

    public void updateComment(Long commentId, String content, Boolean isAffected){
        //Dirty checking을 활용하여, 엔티티를 가져오고 수정한다.
            Comment comment = commentRepository.getReferenceById(commentId);
            if(comment.getContent() != null ) {
                comment.setContent(content);
            }
            if(comment.getIsAffected() != null){
                comment.setIsAffected(isAffected);
            }
    }


}
