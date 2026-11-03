package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class FichierServiceTest {
    @TempDir Path dossier;

    @Test void refuseUnFichierQuiUsurpeUneExtensionImage() {
        FichierService service = new FichierService(dossier.toString());
        MockMultipartFile faux = new MockMultipartFile("cin", "cin.png", "image/png", "du texte".getBytes());
        assertThatThrownBy(() -> service.stocker(faux)).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("contenu du fichier");
    }

    @Test void accepteUneVraieSignaturePng() {
        FichierService service = new FichierService(dossier.toString());
        byte[] png = { (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x01 };
        MockMultipartFile image = new MockMultipartFile("cin", "cin.png", "image/png", png);
        FichierService.Fichier enregistre = service.stocker(image);
        assertThat(enregistre.format()).isEqualTo("png");
        assertThat(service.charger(enregistre.chemin()).exists()).isTrue();
    }
}
