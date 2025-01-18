package org.gdgoc.server.client;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationStateRepository extends JpaRepository<ConversationState, String> {
    Optional<ConversationState> findByUserId(String userId);
}
