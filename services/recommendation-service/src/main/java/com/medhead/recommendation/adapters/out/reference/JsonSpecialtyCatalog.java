package com.medhead.recommendation.adapters.out.reference;

import com.medhead.recommendation.application.port.SpecialtyCatalog;
import com.medhead.recommendation.domain.Specialty;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptateur sortant : lit le référentiel {@code contracts/reference/specialties.json} une seule fois, au démarrage.
 */
@Component
public class JsonSpecialtyCatalog implements SpecialtyCatalog {

    private static final TypeReference<List<SpecialtyEntry>> ENTRIES = new TypeReference<>() {
    };

    private final List<Specialty> specialties;

    public JsonSpecialtyCatalog(SpecialtiesReferenceProperties properties, JsonMapper jsonMapper) {
        this.specialties = load(
                Objects.requireNonNull(properties.specialtiesFile(), "medhead.recommendation-service.specialties-file"),
                jsonMapper);
    }

    @Override
    public List<Specialty> findAll() {
        return specialties;
    }

    private static List<Specialty> load(Resource resource, JsonMapper jsonMapper) {
        List<SpecialtyEntry> entries;
        try (InputStream in = resource.getInputStream()) {
            entries = jsonMapper.readValue(in, ENTRIES);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read specialties reference " + resource.getDescription(), e);
        }
        List<Specialty> result = entries.stream().map(e -> new Specialty(e.code(), e.group(), e.label())).toList();
        Set<String> codes = new HashSet<>();
        for (Specialty specialty : result) {
            if (!codes.add(specialty.code())) {
                throw new IllegalStateException("Duplicate specialty code in reference: " + specialty.code());
            }
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("Specialties reference is empty: " + resource.getDescription());
        }
        return result;
    }

    private record SpecialtyEntry(String code, String group, String label) {
    }
}
