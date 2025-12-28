package org.hanseiro.server.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseiro.server.domain.user.model.SocialProvider;

@Entity
@Table( name = "oauth_accounts" )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OAuthAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SocialProvider provider;

    // 구글 userinfo.id, id_token.sub
    @Column(nullable = false, length = 100)
    private String providerSubject;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
