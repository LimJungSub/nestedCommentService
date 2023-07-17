package com.myfirstspringproject.Repository;

import com.myfirstspringproject.Domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

//JpaRepository의 구현체 안에 이미 @Repository가 있어 어노테이션을 붙일 필요가 없다.
@RepositoryRestResource
public interface commentRepository extends JpaRepository<Comment, Long>
{
}
