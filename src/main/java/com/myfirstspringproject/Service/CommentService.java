package com.myfirstspringproject.Service;

import com.myfirstspringproject.Domain.Comment;
import com.myfirstspringproject.Dto.CommentDto;
import com.myfirstspringproject.Repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Page<CommentDto> getComments(Pageable pageable){
        //바로 entity로 내보내면 안되므로 dto로 변환하는 로직 필요 , 서비스에서 구현하자.
        return commentRepository.findAll(pageable) //page로 싸져있는데 안까도되나? 여기서 의미상 컬렉션으로 리턴될텐데... 음?
                .map(CommentDto::toDto);
    }


}
