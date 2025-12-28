package org.hanseiro.server.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table( name = "users" )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = true, length = 100)
    private String department;

    @Column(nullable = true, length = 100)
    private String name;

    public void updateProfile(String department, String name) {
        this.department = department;
        this.name = name;
    }
}
