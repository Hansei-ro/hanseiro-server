package org.hanseiro.server.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseiro.server.global.model.BaseTimeEntity;

@Entity
@Table( name = "users" )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserEntity extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = true, length = 100)
  private String major;

  @Column(nullable = true, length = 100)
  private String name;

  public void updateProfile(String major, String name) {
    this.major = major;
    this.name = name;
  }
}