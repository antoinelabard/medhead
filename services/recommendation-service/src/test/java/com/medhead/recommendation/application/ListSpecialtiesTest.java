package com.medhead.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.medhead.recommendation.domain.Specialty;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListSpecialtiesTest {

    @Test
    void listsSpecialtiesInCatalogOrder() {
        List<Specialty> catalog = List.of(
                new Specialty("vascular-surgery", "Groupe chirurgical", "Chirurgie vasculaire"),
                new Specialty("anaesthetics", "Anesthésie", "Anesthésie"));

        List<Specialty> result = new ListSpecialties(() -> catalog).list();

        assertThat(result).containsExactlyElementsOf(catalog);
    }

    @Test
    void returnsAnImmutableList() {
        List<Specialty> catalog = new ArrayList<>(List.of(new Specialty("anaesthetics", "Anesthésie", "Anesthésie")));

        List<Specialty> result = new ListSpecialties(() -> catalog).list();

        assertThatThrownBy(result::clear).isInstanceOf(UnsupportedOperationException.class);
    }
}
