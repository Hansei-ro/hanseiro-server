package org.hanseiro.server.domain.user.repository;

import org.hanseiro.server.domain.user.model.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    void deleteAllByUser_Id(Long userId);
    long countByUser_Id(Long userId);
}
