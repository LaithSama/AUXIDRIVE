package mg.auxidrive.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class FichierExceptionHandlerTest {

    @Test void unFichierTropVolumineuxRepond413AvecUnMessageClair() {
        var reponse = new FichierExceptionHandler()
                .fichierTropVolumineux(new MaxUploadSizeExceededException(5L * 1024 * 1024));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(reponse.getBody()).containsKey("message");
        assertThat(reponse.getBody().get("message")).contains("5 Mo");
    }
}
