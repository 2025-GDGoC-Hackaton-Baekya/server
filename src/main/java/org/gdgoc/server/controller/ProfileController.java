package org.gdgoc.server.controller;

import lombok.RequiredArgsConstructor;
import org.gdgoc.server.domain.Profile;
import org.gdgoc.server.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  /**
   * 프로필 조회
   */
  @GetMapping("/{signid}")
  public ResponseEntity<Profile> getProfile(@PathVariable String signid) {
    Profile profile = profileService.getProfile(signid);
    return ResponseEntity.ok(profile);
  }

  /**
   * 프로필 생성 또는 수정
   */
  @PostMapping("/{signid}")
  public ResponseEntity<Profile> createOrUpdateProfile(
      @PathVariable String signid,
      @RequestBody Profile profileRequest) {
    Profile profile = profileService.createOrUpdateProfile(signid, profileRequest);
    return ResponseEntity.ok(profile);
  }

  /**
   * 특정 테마 수정
   */
  @PatchMapping("/{signid}/theme")
  public ResponseEntity<Profile> updateTheme(
      @PathVariable String signid,
      @RequestParam String themeType,
      @RequestParam String themeValue) {
    Profile profile = profileService.updateTheme(signid, themeType, themeValue);
    return ResponseEntity.ok(profile);
  }

  /**
   * 프로필 삭제
   */
  @DeleteMapping("/{signid}")
  public ResponseEntity<Void> deleteProfile(@PathVariable String signid) {
    profileService.deleteProfile(signid);
    return ResponseEntity.noContent().build();
  }
}
