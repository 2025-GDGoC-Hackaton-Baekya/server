package org.gdgoc.server.repository;

import org.gdgoc.server.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

}
