package org.gdgoc.server.service;

import lombok.RequiredArgsConstructor;
import org.gdgoc.server.domain.Profile;
import org.gdgoc.server.domain.User;
import org.gdgoc.server.repository.ProfileRepository;
import org.gdgoc.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final UserRepository userRepository; // signid로 사용자 검색

  /**
   * 사용자 프로필 조회
   */
  public Profile getProfile(String signid) {
    return profileRepository.findByUser_Signid(signid)
        .orElseThrow(() -> new IllegalArgumentException("Profile not found for signid: " + signid));
  }

  /**
   * 프로필 생성 또는 수정
   */
  @Transactional
  public Profile createOrUpdateProfile(String signid, Profile profileRequest) {
    User user = userRepository.findBySignid(signid)
        .orElseThrow(() -> new IllegalArgumentException("User not found with signid: " + signid));

    Profile profile = profileRepository.findByUser_Signid(signid)
        .orElse(new Profile());

    profile.setUser(user);
    profile.setSlogan(profileRequest.getSlogan());
    profile.setAboutMe(profileRequest.getAboutMe());
    profile.setWork(profileRequest.getWork());
    profile.setInterest(profileRequest.getInterest());
    profile.setExperience(profileRequest.getExperience());
    profile.setMessage(profileRequest.getMessage());

    return profileRepository.save(profile);
  }

  /**
   * 특정 테마 수정
   */
  @Transactional
  public Profile updateTheme(String signid, String themeType, String themeValue) {
    Profile profile = profileRepository.findByUser_Signid(signid)
        .orElseThrow(() -> new IllegalArgumentException("Profile not found for signid: " + signid));

    switch (themeType.toLowerCase()) {
      case "slogan":
        profile.setSlogan(themeValue);
        break;
      case "aboutme":
        profile.setAboutMe(themeValue);
        break;
      case "work":
        profile.setWork(themeValue);
        break;
      case "interest":
        profile.setInterest(themeValue);
        break;
      case "experience":
        profile.setExperience(themeValue);
        break;
      case "message":
        profile.setMessage(themeValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid theme type: " + themeType);
    }

    return profileRepository.save(profile);
  }

  /**
   * 프로필 삭제
   */
  @Transactional
  public void deleteProfile(String signid) {
    Profile profile = profileRepository.findByUser_Signid(signid)
        .orElseThrow(() -> new IllegalArgumentException("Profile not found for signid: " + signid));
    profileRepository.delete(profile);
  }
}
