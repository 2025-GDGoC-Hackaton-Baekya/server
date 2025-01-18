package org.gdgoc.server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
}

