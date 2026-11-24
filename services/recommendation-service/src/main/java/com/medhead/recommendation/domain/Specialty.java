package com.medhead.recommendation.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Spécialité médicale du référentiel NHS.
 *
 * @param code  code stable en kebab-case anglais, par exemple {@code emergency-medicine}
 * @param group groupe de spécialité, en français
 * @param label libellé de la spécialité, en français
 */
public record Specialty(String code, String group, String label) {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    public Specialty {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(label, "label");
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("Specialty code must be kebab-case: " + code);
        }
        if (group.isBlank()) {
            throw new IllegalArgumentException("Specialty group must not be blank for " + code);
        }
        if (label.isBlank()) {
            throw new IllegalArgumentException("Specialty label must not be blank for " + code);
        }
    }
}
