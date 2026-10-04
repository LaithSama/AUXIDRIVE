package mg.auxidrive.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Le code tarifaire d'un véhicule pilote l'application du TACAVA : si le JSON ne permet plus de
 * le renseigner, la prime est calculée sans TACAVA, silencieusement, sans qu'aucune erreur ne
 * soit signalée. Ce test verrouille donc que {@code codeTarif} reste désérialisable depuis le
 * corps de la requête.
 */
class VehiculeCodeTarifJsonTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void leCodeTarifEstDeserialisableDepuisLeJson() throws Exception {
        String json = """
                {"numImmatriculation":"9999-AA-99","numChassis":"VF1JSONTEST0001","marque":"Renault",
                 "modele":"Clio","puissanceFiscale":5,"energie":"Essence","nombrePlaces":5,
                 "codeTarif":{"idCodeAuto":1}}
                """;

        Vehicule vehicule = objectMapper.readValue(json, Vehicule.class);

        assertNotNull(vehicule.getCodeTarif(), "codeTarif doit etre renseigne par la requete");
        assertEquals(1L, vehicule.getCodeTarif().getIdCodeAuto());
    }

    @Test
    void leVehiculeResteDeserialisableSansCodeTarif() throws Exception {
        String json = """
                {"numImmatriculation":"9998-AA-98","numChassis":"VF1JSONTEST0002","marque":"Peugeot",
                 "modele":"208","puissanceFiscale":6,"energie":"Essence","nombrePlaces":5}
                """;

        Vehicule vehicule = objectMapper.readValue(json, Vehicule.class);

        assertNotNull(vehicule.getNumImmatriculation());
        org.junit.jupiter.api.Assertions.assertNull(vehicule.getCodeTarif());
    }
}