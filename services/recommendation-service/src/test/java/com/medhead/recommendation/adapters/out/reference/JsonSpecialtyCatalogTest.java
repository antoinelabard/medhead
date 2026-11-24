package com.medhead.recommendation.adapters.out.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.medhead.recommendation.domain.Specialty;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import tools.jackson.databind.json.JsonMapper;

class JsonSpecialtyCatalogTest {

    private static final Path CONTRACTS = Path.of(System.getProperty("medhead.contracts.dir", "../../contracts"));
    private static final Path SPECS = Path.of(System.getProperty("medhead.specs.dir", "../../specs"));

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void sharedReferenceHasOneEntryPerNhsTableRowWithUniqueCodes() throws IOException {
        List<Specialty> specialties = sharedCatalog().findAll();

        assertThat(specialties).hasSize(nhsTableRows().size());
        assertThat(specialties).extracting(Specialty::code).doesNotHaveDuplicates();
    }

    @Test
    void sharedReferenceKeepsGroupsAndLabelsOfTheNhsTableInOrder() throws IOException {
        List<String> expected = nhsTableRows();

        List<String> actual = sharedCatalog().findAll().stream()
                .map(s -> s.group() + " | " + s.label())
                .toList();

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void rejectsDuplicateCodes() {
        String json = """
                [{"code":"cardiology","group":"G","label":"A"},{"code":"cardiology","group":"G","label":"B"}]
                """;

        assertThatIllegalStateException().isThrownBy(() -> catalogOf(json)).withMessageContaining("cardiology");
    }

    @Test
    void rejectsEmptyReference() {
        assertThatIllegalStateException().isThrownBy(() -> catalogOf("[]"));
    }

    private JsonSpecialtyCatalog sharedCatalog() {
        return new JsonSpecialtyCatalog(new SpecialtiesReferenceProperties(
                new FileSystemResource(CONTRACTS.resolve("reference/specialties.json"))),
                jsonMapper);
    }

    private JsonSpecialtyCatalog catalogOf(String json) {
        return new JsonSpecialtyCatalog(new SpecialtiesReferenceProperties(
                new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8))), jsonMapper);
    }

    /** Lignes « groupe | spécialité » de la table NHS de référence, dans l'ordre. */
    private static List<String> nhsTableRows() throws IOException {
        return Files.readAllLines(SPECS.resolve("Données_de_référence_sur_les_spécialités_NHS.md"),
                        StandardCharsets.UTF_8).stream()
                .filter(line -> line.startsWith("| ") && !line.startsWith("| Groupe de spécialité"))
                .map(line -> line.substring(1, line.length() - 1).split("\\|"))
                .map(cells -> cells[0].strip() + " | " + cells[1].strip())
                .toList();
    }
}
