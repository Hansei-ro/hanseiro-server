package org.hanseiro.server.domain.user.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(length = 100)
    private String name;

    protected UserEntity() {}

    private UserEntity(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public static UserEntity create(String email, String name) {
        return new UserEntity(email, name);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}