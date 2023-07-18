package com.myfirstspringproject.Repository;

import com.myfirstspringproject.Domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface userAccountRepository extends JpaRepository<UserAccount,Long> {
    public Optional<UserAccount> findByUserId(String userId);
}
