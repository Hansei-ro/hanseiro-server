package org.hanseiro.server.domain.user.validator.parser;

import org.springframework.stereotype.Component;

@Component
public class UserNameParser {

    public ParsedUserName parse(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return new ParsedUserName(null, null);
        }

        String[] parts = rawName.split("/", 2);
        if (parts.length == 2) {
            return new ParsedUserName(
                    trimToNull(parts[0]),
                    trimToNull(parts[1])
            );
        }

        return new ParsedUserName(null, rawName.trim());
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record ParsedUserName(String department, String name) {}
}

