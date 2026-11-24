package com.medhead.recommendation;

import com.medhead.recommendation.application.ListSpecialties;
import com.medhead.recommendation.application.port.SpecialtyCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemble les cas d'usage, qui restent indépendants de Spring.
 */
@Configuration(proxyBeanMethods = false)
class ApplicationConfiguration {

    @Bean
    ListSpecialties listSpecialties(SpecialtyCatalog specialtyCatalog) {
        return new ListSpecialties(specialtyCatalog);
    }
}
