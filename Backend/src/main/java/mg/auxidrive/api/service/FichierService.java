package mg.auxidrive.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class FichierService {

    public record Fichier(String chemin, String format, int tailleKo) {
    }

    private static final long TAILLE_MAX = 5L * 1024 * 1024;
    private static final Map<String, String> TYPES_MIME = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png");

    private final Path racine;

    public FichierService(@Value("${app.upload.dir:uploads}") String dossier) {
        this.racine = Path.of(dossier).toAbsolutePath().normalize();
        try {
            Files.createDirectories(racine);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le dossier d'upload : " + racine, e);
        }
    }

    public Fichier stockerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier vide");
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier trop volumineux (5 Mo maximum)");
        }
        String extension = extensionFichier(fichier.getOriginalFilename());
        if (!TYPES_MIME.containsKey(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format non accepté (PDF, JPEG ou PNG)");
        }
        // Nom généré
        String nom = UUID.randomUUID() + "." + extension;
        try (InputStream in = fichier.getInputStream()) {
            Files.copy(in, racine.resolve(nom));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Enregistrement du fichier impossible");
        }
        return new Fichier(nom, extension, (int) Math.ceil(fichier.getSize() / 1024.0));
    }

    public void supprimerFichier(String chemin) {
        if (chemin == null) {
            return;
        }
        Path fichier = racine.resolve(chemin).normalize();
        if (fichier.startsWith(racine)) {
            try {
                Files.deleteIfExists(fichier);
            } catch (IOException e) {
                // le fichier restera sur le disque, sans conséquence pour la base
            }
        }
    }

    public Resource chargerFichier(String chemin) {
        Path fichier = racine.resolve(chemin).normalize();
        if (!fichier.startsWith(racine) || !Files.exists(fichier)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
        }
        return new FileSystemResource(fichier);
    }

    public String typeMime(String format) {
        return format == null ? "application/octet-stream"
                : TYPES_MIME.getOrDefault(format.toLowerCase(), "application/octet-stream");
    }

    private String extensionFichier(String nomOriginal) {
        if (nomOriginal == null || !nomOriginal.contains(".")) {
            return "";
        }
        return nomOriginal.substring(nomOriginal.lastIndexOf('.') + 1).toLowerCase();
    }
}
