package org.hanseiro.server.domain.user.repository;


import org.hanseiro.server.domain.user.model.SocialProvider;
import org.hanseiro.server.domain.user.model.entity.OAuthAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccountEntity, Long> {
    Optional<OAuthAccountEntity> findByProviderAndProviderSubject(
            SocialProvider provider,
            String providerSubject
    );

    boolean existsByProviderAndProviderSubject(SocialProvider provider, String providerSubject);
}
