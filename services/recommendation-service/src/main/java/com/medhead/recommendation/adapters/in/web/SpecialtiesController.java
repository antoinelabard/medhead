package com.medhead.recommendation.adapters.in.web;

import com.medhead.recommendation.adapters.in.web.api.SpecialtiesApi;
import com.medhead.recommendation.application.ListSpecialties;
import com.medhead.recommendation.domain.Specialty;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST implémentant l'interface générée depuis le contrat OpenAPI.
 */
@RestController
class SpecialtiesController implements SpecialtiesApi {

    private final ListSpecialties listSpecialties;

    SpecialtiesController(ListSpecialties listSpecialties) {
        this.listSpecialties = listSpecialties;
    }

    @Override
    public ResponseEntity<List<com.medhead.recommendation.adapters.in.web.api.model.Specialty>> listSpecialties() {
        return ResponseEntity.ok(listSpecialties.list().stream().map(SpecialtiesController::toDto).toList());
    }

    private static com.medhead.recommendation.adapters.in.web.api.model.Specialty toDto(Specialty specialty) {
        return new com.medhead.recommendation.adapters.in.web.api.model.Specialty(
                specialty.code(), specialty.group(), specialty.label());
    }
}
