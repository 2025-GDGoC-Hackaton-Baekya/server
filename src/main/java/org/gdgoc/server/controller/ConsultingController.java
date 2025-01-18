package org.gdgoc.server.controller;
import java.time.LocalDate;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.gdgoc.server.domain.Consulting;
import org.gdgoc.server.service.ConsultingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/consulting")
@Slf4j
public class ConsultingController {

  @Autowired
  private ConsultingService consultingService;

  // 컨설팅 등록 (EXPERT만 가능)
  @PostMapping("/register")
  public ResponseEntity<Consulting> registerConsulting(@RequestParam String signid,
      @RequestParam LocalDateTime date,
      @RequestParam String consultant,
      @RequestParam String location) {
    try {
      Consulting consulting = consultingService.registerConsulting(signid, date, consultant, location);
      return ResponseEntity.ok(consulting);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 컨설팅 예약
  @PostMapping("/reserve")
  public ResponseEntity<Consulting> reserveConsulting(@RequestParam String signid,
      @RequestParam Long consultingId) {
    try {
      Consulting consulting = consultingService.reserveConsulting(signid, consultingId);
      return ResponseEntity.ok(consulting);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 특정 날짜에 컨설팅 목록 조회
  @GetMapping("/date")
  public ResponseEntity<List<Consulting>> getConsultingsByDate(@RequestParam LocalDate date) {
    List<Consulting> consultings = consultingService.getConsultingsByDate(date);
    log.info("" + consultings.size());
    return ResponseEntity.ok(consultings);
  }
}

