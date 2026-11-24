package com.medhead.recommendation.adapters.out.reference;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Emplacement du référentiel des spécialités ({@code MEDHEAD_RECOMMENDATION_SERVICE_SPECIALTIES_FILE}).
 *
 * @param specialtiesFile ressource JSON {@code [{code, group, label}]}
 */
@ConfigurationProperties("medhead.recommendation-service")
public record SpecialtiesReferenceProperties(Resource specialtiesFile) {
}
