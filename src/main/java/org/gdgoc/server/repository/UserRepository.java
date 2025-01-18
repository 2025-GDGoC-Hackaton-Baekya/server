package org.gdgoc.server.repository;

import java.util.Optional;
import org.gdgoc.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  // signid와 password를 기준으로 사용자 찾기
  Optional<User> findBySignidAndPassword(String signid, String password);

}

