package com.medhead.recommendation.application.port;

import com.medhead.recommendation.domain.Specialty;
import java.util.List;

/**
 * Port sortant : accès au référentiel des spécialités NHS.
 */
public interface SpecialtyCatalog {

    /**
     * Renvoie toutes les spécialités du référentiel.
     *
     * @return liste immuable, dans l'ordre du référentiel
     */
    List<Specialty> findAll();
}
