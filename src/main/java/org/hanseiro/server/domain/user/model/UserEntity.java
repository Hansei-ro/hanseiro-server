package org.hanseiro.server.domain.user.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String department;

    public static UserEntity create(String email) {
        return UserEntity.builder()
                .email(email == null ? null : email.trim().toLowerCase())
                .build();
    }

    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
}