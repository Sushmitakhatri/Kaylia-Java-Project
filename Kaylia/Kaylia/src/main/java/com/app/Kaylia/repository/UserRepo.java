package com.app.Kaylia.repository;

import com.app.Kaylia.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findUserByEmail(String email);

    User findByEmail(String email);

    void removeUserByUserId(int id);
}
