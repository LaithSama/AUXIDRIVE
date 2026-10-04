package mg.auxidrive.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean actif;
    private final String expediteur;

    public MailService(ObjectProvider<JavaMailSender> mailSender,
                       @Value("${app.mail.enabled:false}") boolean actif,
                       @Value("${spring.mail.username:}") String expediteur) {
        this.mailSender = mailSender;
        this.actif = actif;
        this.expediteur = expediteur;
    }

    /**
     * Envoie les identifiants au client. Tant que app.mail.enabled vaut false (développement),
     * le mot de passe temporaire est affiché dans la console au lieu d'être envoyé.
     */
    public void envoyerIdentifiants(String destinataire, String motDePasseTemporaire) {
        if (!actif) {
            log.warn("[DEV] Mail désactivé. Identifiants de {} : mot de passe temporaire = {}",
                    destinataire, motDePasseTemporaire);
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("app.mail.enabled=true mais spring.mail.host n'est pas configuré");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (!expediteur.isBlank()) {
            message.setFrom(expediteur);
        }
        message.setTo(destinataire);
        message.setSubject("AUXIASSUR - Votre inscription est validée");
        message.setText("Bonjour,\n\n"
                + "Votre inscription sur AUXIASSUR a été validée.\n\n"
                + "Identifiant : " + destinataire + "\n"
                + "Mot de passe temporaire : " + motDePasseTemporaire + "\n\n"
                + "Cordialement,\nAUXIASSUR");
        sender.send(message);
    }
}
