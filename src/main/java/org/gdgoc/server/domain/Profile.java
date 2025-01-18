package org.gdgoc.server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gdgoc.server.client.RetirementCounselingService.ProfileResponse;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_signid", referencedColumnName = "signid")
  private User user; // User의 signid를 참조


  private String slogan;

  private String aboutMe;
  private String work;
  private String interest;
  private String experience;
  private String message;

  public static Profile from(User user, ProfileResponse response) {
    return Profile.builder()
            .user(user)
            .slogan(response.getSlogan())
            .aboutMe(response.getAboutMe())
            .work(response.getWork())
            .interest(response.getInterest())
            .experience(response.getExperience())
            .message(response.getMessage())
            .build();
  }

  @Builder
  public Profile(User user, String slogan, String aboutMe, String work, String interest, String experience,
                 String message) {
    this.user = user;
    this.slogan = slogan;
    this.aboutMe = aboutMe;
    this.work = work;
    this.interest = interest;
    this.experience = experience;
    this.message = message;
  }
}

