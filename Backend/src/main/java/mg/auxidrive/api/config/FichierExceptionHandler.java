package mg.auxidrive.api.config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * Réponse homogène pour les envois de pièces dépassant la limite du serveur
 * (5 Mo par fichier) : le client reçoit un 413 explicite au lieu d'une erreur 500.
 */
@RestControllerAdvice
public class FichierExceptionHandler {

    @ExceptionHandler({ MaxUploadSizeExceededException.class, MultipartException.class })
    public ResponseEntity<Map<String, String>> fichierTropVolumineux(MultipartException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("message", "Fichier trop volumineux ou requête multipart invalide (5 Mo maximum par fichier)."));
    }
}
