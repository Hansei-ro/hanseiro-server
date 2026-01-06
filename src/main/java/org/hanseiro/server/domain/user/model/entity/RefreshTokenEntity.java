package org.hanseiro.server.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table( name = "refresh_tokens" )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder

public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //해시값만 저장
    @Column(nullable = false, length = 255, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}

