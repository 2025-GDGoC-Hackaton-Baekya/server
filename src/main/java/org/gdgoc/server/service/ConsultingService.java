package org.gdgoc.server.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import org.gdgoc.server.domain.Consulting;
import org.gdgoc.server.domain.User;
import org.gdgoc.server.repository.ConsultingRepository;
import org.gdgoc.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ConsultingService {

  @Autowired
  private ConsultingRepository consultingRepository;

  @Autowired
  private UserRepository userRepository;

  // 컨설팅 등록 (EXPERT만 가능)
  @Transactional
  public Consulting registerConsulting(String signid, LocalDateTime date, String consultant, String location) {
    User user = userRepository.findBySignid(signid)
        .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

    if (user.getRole() != User.Role.EXPERT) {
      throw new IllegalArgumentException("EXPERT만 컨설팅을 등록할 수 있습니다.");
    }

    Consulting consulting = new Consulting();
    consulting.setUser(user);
    consulting.setDate(date);
    consulting.setConsultant(consultant);
    consulting.setLocation(location);
    consulting.setSoldOut(false); // 초기 상태는 예약 가능

    return consultingRepository.save(consulting);
  }

  // 컨설팅 예약
  @Transactional
  public Consulting reserveConsulting(String signid, Long consultingId) {
    User user = userRepository.findBySignid(signid)
        .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    if (user == null) {
      throw new IllegalArgumentException("사용자가 존재하지 않습니다.");
    }

    Consulting consulting = consultingRepository.findByIdAndSoldOutFalse(consultingId);
    if (consulting == null) {
      throw new IllegalArgumentException("해당 컨설팅은 이미 예약이 완료되었습니다.");
    }

    consulting.setSoldOut(true); // 예약 완료
    consulting.setUser(user);   // 예약한 사용자 정보 저장

    return consultingRepository.save(consulting);
  }

  // 특정 날짜에 컨설팅 목록 조회
  public List<Consulting> getConsultingsByDate(LocalDate date) {
    LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);  // 해당 날짜 00:00
    LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);    // 해당 날짜 23:59:59.999999

    return consultingRepository.findAllByDateBetween(startOfDay, endOfDay); // 시작과 끝 날짜로 조회
  }
}


