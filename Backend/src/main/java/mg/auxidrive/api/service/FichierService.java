package mg.auxidrive.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Stockage des pièces justificatives sur le disque du serveur (PDF, JPEG, PNG, 5 Mo max). */
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

    public Fichier stocker(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier vide");
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier trop volumineux (5 Mo maximum)");
        }
        String extension = extension(fichier.getOriginalFilename());
        if (!TYPES_MIME.containsKey(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format non accepté (PDF, JPEG ou PNG)");
        }
        // Nom généré : on n'utilise jamais le nom envoyé par le client
        String nom = UUID.randomUUID() + "." + extension;
        try (InputStream in = fichier.getInputStream()) {
            Files.copy(in, racine.resolve(nom));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Enregistrement du fichier impossible");
        }
        return new Fichier(nom, extension, (int) Math.ceil(fichier.getSize() / 1024.0));
    }

    /** Supprime un fichier stocké (sans erreur s'il n'existe plus). */
    public void supprimer(String chemin) {
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

    public Resource charger(String chemin) {
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

    private String extension(String nomOriginal) {
        if (nomOriginal == null || !nomOriginal.contains(".")) {
            return "";
        }
        return nomOriginal.substring(nomOriginal.lastIndexOf('.') + 1).toLowerCase();
    }
}
