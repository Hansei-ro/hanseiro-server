package org.hanseiro.server.domain.user.validator;

import org.springframework.stereotype.Component;

@Component
public class UserNameParser {
    public ParsedUserName parse(String rawName) {
        if (rawName == null) return new ParsedUserName(null, null);

        String trimmed = rawName.trim();
        if (trimmed.isEmpty()) return new ParsedUserName(null, null);

        String[] parts = trimmed.split("/", 2);
        String name = toNull(parts[0]);
        String dept = (parts.length == 2) ? toNull(parts[1]) : null;

        return new ParsedUserName(name, dept);
    }

    private String toNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public record ParsedUserName(String name, String department) {}
}
