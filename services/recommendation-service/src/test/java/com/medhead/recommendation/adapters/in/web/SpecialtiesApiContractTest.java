package com.medhead.recommendation.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Vérifie la réponse réelle de {@code GET /api/v1/specialties} contre le schéma du contrat OpenAPI.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpecialtiesApiContractTest {

    private static final Path CONTRACTS = Path.of(System.getProperty("medhead.contracts.dir", "../../contracts"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void responseMatchesTheContractAndTheSharedReference() throws Exception {
        String body = mockMvc.perform(get("/api/v1/specialties").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        JsonNode response = jsonMapper.readTree(body);
        List<Error> errors = responseSchema().validate(response);

        assertThat(errors).isEmpty();
        assertThat(response).isEqualTo(jsonMapper.readTree(CONTRACTS.resolve("reference/specialties.json").toFile()));
    }

    @Test
    void contractSchemaRejectsAnItemThatBreaksTheContract() throws Exception {
        JsonNode invalid = jsonMapper.readTree("""
                [{"code": "Emergency Medicine", "group": "Médecine d'urgence"}]
                """);

        assertThat(responseSchema().validate(invalid)).isNotEmpty();
    }

    /** Schéma de la réponse 200, avec les {@code components} du contrat pour résoudre les références. */
    private Schema responseSchema() throws IOException {
        JsonNode contract = YAMLMapper.builder().build()
                .readTree(Files.readString(CONTRACTS.resolve("openapi/medhead-api.yaml")));
        var schema = contract.at("/paths/~1api~1v1~1specialties/get/responses/200/content/application~1json/schema")
                .deepCopy();
        var root = jsonMapper.createObjectNode();
        root.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        root.setAll((tools.jackson.databind.node.ObjectNode) schema);
        root.set("components", contract.get("components"));
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(root);
    }
}
