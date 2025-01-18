package org.gdgoc.server.repository;

import java.util.Optional;
import org.gdgoc.server.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
  Optional<Profile> findByUser_Signid(String signid);
}

