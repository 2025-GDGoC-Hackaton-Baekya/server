package org.gdgoc.server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private int age;
    private int gender;
    private String birth;
    private String signid;
    private String password;
  private String phone;
    @Enumerated(value= EnumType.STRING)
    private Role role;

    public enum Role {
        NORMAL,
        EXPERT
    }

    public User() {
        this.role = Role.NORMAL;
    }
}
