package com.educampus.academic.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Vérifie que la copie du XSD utilisée par le client SOAP d'Academic est identique au contrat
 * source publié par University Administration. Si les deux divergent, le test échoue.
 * Le test est ignoré si le dépôt complet n'est pas présent (ex. build Docker d'un seul service).
 */
class XsdContractSyncTest {

    private static final Path LOCAL_XSD =
            Path.of("src", "main", "resources", "xsd", "university-administration.xsd");
    private static final Path SOURCE_XSD = Path.of("..", "..", "projet_SOA-feature-admin-data-model",
            "university-administration", "src", "main", "resources", "xsd", "university-administration.xsd");

    @Test
    void copieDuXsdIdentiqueAuContratDeLAdministration() throws IOException {
        assumeTrue(Files.exists(SOURCE_XSD), "Contrat source absent : test ignoré");
        assertThat(Files.readString(LOCAL_XSD))
                .as("Le XSD copié dans academic-service doit rester identique à celui de University Administration")
                .isEqualTo(Files.readString(SOURCE_XSD));
    }
}
