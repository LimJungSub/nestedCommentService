package com.myfirstspringproject.Repository;

import com.myfirstspringproject.Domain.Comment;
import com.myfirstspringproject.Dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

//JpaRepository의 구현체 안에 이미 @Repository가 있어 어노테이션을 붙일 필요가 없다.
@RepositoryRestResource
public interface CommentRepository extends JpaRepository<Comment, Long>
{
    @Override
    Page<Comment> findAll(Pageable pageable);

    Page<Comment> findByParentIdIsNull(Pageable pageable);
    List<Comment> findByParentIdIsNull();
//    Page<CommentDto> findByParentCommentIsNull(Pageable pageable);
}
