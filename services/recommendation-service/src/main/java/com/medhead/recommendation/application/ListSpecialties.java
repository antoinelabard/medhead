package com.medhead.recommendation.application;

import com.medhead.recommendation.application.port.SpecialtyCatalog;
import com.medhead.recommendation.domain.Specialty;
import java.util.List;
import java.util.Objects;

/**
 * Cas d'usage : lister les spécialités NHS proposées à la sélection.
 */
public class ListSpecialties {

    private final SpecialtyCatalog specialtyCatalog;

    public ListSpecialties(SpecialtyCatalog specialtyCatalog) {
        this.specialtyCatalog = Objects.requireNonNull(specialtyCatalog, "specialtyCatalog");
    }

    /**
     * Liste les spécialités dans l'ordre du référentiel.
     *
     * @return liste immuable des spécialités
     */
    public List<Specialty> list() {
        return List.copyOf(specialtyCatalog.findAll());
    }
}
