package com.medhead.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SpecialtyTest {

    @Test
    void keepsItsValues() {
        Specialty specialty = new Specialty("emergency-medicine", "Médecine d'urgence", "Médecine d'urgence");

        assertThat(specialty.code()).isEqualTo("emergency-medicine");
        assertThat(specialty.group()).isEqualTo("Médecine d'urgence");
        assertThat(specialty.label()).isEqualTo("Médecine d'urgence");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Emergency", "emergency_medicine", "-cardiology", "cardiology-", "a--b", "é"})
    void rejectsCodeThatIsNotKebabCase(String code) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Specialty(code, "Groupe", "Libellé"));
    }

    @Test
    void rejectsBlankGroupOrLabel() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Specialty("cardiology", " ", "Cardiologie"));
        assertThatIllegalArgumentException().isThrownBy(() -> new Specialty("cardiology", "Groupe", ""));
    }

    @Test
    void rejectsNullFields() {
        assertThatNullPointerException().isThrownBy(() -> new Specialty(null, "Groupe", "Libellé"));
        assertThatNullPointerException().isThrownBy(() -> new Specialty("cardiology", null, "Libellé"));
        assertThatNullPointerException().isThrownBy(() -> new Specialty("cardiology", "Groupe", null));
    }
}
