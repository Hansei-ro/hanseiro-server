package org.hanseiro.server.domain.user.service.google;

import lombok.RequiredArgsConstructor;
import org.hanseiro.server.domain.user.model.UserEntity;
import org.hanseiro.server.domain.user.repository.UserRepository;
import org.hanseiro.server.domain.user.validator.UserNameParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleUserUpsertService {
    private final UserRepository userRepository;

    @Transactional
    public UpsertResult upsert(String email, UserNameParser.ParsedUserName parsed) {
        // 신규 가입 여부 확인 (저장 전에 체크)
        boolean isNew = !userRepository.existsByEmail(email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(UserEntity.create(email)));

        if (parsed != null) {
            if (parsed.name() != null) user.setName(parsed.name());
            if (parsed.department() != null) user.setDepartment(parsed.department());
        }

        return new UpsertResult(user, isNew);
    }

    public record UpsertResult(UserEntity user, boolean isNew) {}
}
