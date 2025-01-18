package org.gdgoc.server.repository;

import org.gdgoc.server.domain.Consulting;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface ConsultingRepository extends JpaRepository<Consulting, Long> {

  @Query("select c from Consulting c where c.date between :startOfDay and :endOfDay")
  List<Consulting> findAllByDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

  Consulting findByIdAndSoldOutFalse(Long id); // 솔드아웃되지 않은 컨설팅을 조회
}


