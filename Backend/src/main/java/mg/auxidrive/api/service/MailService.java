package mg.auxidrive.api.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean actif;
    private final String expediteur;

    /** Données du mail J-3, déjà extraites : l'envoi est asynchrone, aucune entité n'y accède. */
    public record DonneesEcheance(String destinataire,
                                  String civilite, String nom, String prenom,
                                  String numPolice, LocalDate dateEffet, LocalDate dateExpiration,
                                  Double primeTtc, String vehicules) {
    }

    /** Données du mail de confirmation de renouvellement, déjà extraites. */
    public record DonneesRenouvellement(String destinataire,
                                        String civilite, String nom, String prenom,
                                        String numPolice, LocalDate dateEffet, LocalDate dateExpiration,
                                        Double primeTtc) {
    }

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
    @Async
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
    message.setSubject("AUXIDRIVE - Votre inscription est validée");
        message.setText("Bonjour,\n\n"
                + "Votre inscription sur AUXIDRIVE a été validée.\n\n"
                + "Identifiant : " + destinataire + "\n"
                + "Mot de passe temporaire : " + motDePasseTemporaire + "\n\n"
                + "Cordialement,\nAUXIDRIVE");
        try {
            sender.send(message);
            log.info("Mail d'identifiants envoyé à {}", destinataire);
        } catch (MailException e) {
            log.error("Envoi du mail d'identifiants à {} impossible (validation maintenue)", destinataire, e);
        }
    }

    /**
     * Rappel envoyé trois jours avant l'échéance du contrat : il rappelle les infos du contrat
     * et annonce le renouvellement automatique le cas échéant.
     */
    @Async
    public void envoyerEcheanceProche(DonneesEcheance d) {
        String corps = "Bonjour " + identite(d.civilite(), d.nom(), d.prenom()) + ",\n\n"
                + "Votre contrat n° " + d.numPolice() + " arrive à échéance le " + DATE.format(d.dateExpiration()) + ".\n\n"
                + "- Période : du " + DATE.format(d.dateEffet()) + " au " + DATE.format(d.dateExpiration()) + "\n"
                + "- Prime TTC : " + montant(d.primeTtc()) + "\n"
                + "- Véhicule(s) : " + d.vehicules() + "\n\n"
                + "Pensez à organiser le renouvellement de votre contrat. Si votre contrat bénéficie du\n"
                + "renouvellement automatique, il sera prolongé pour la période suivante sans action de\n"
                + "votre part et vous recevrez une confirmation par e-mail.\n\n"
                + "Cordialement,\nAUXIASSUR";
        envoyer(d.destinataire(), "AUXIASSUR - Votre contrat n° " + d.numPolice() + " arrive à échéance", corps);
    }

    /** Confirmation envoyée après une reconduction tacite : nouvelles dates et prime recalculée. */
    @Async
    public void envoyerRenouvellement(DonneesRenouvellement d) {
        String corps = "Bonjour " + identite(d.civilite(), d.nom(), d.prenom()) + ",\n\n"
                + "Votre contrat n° " + d.numPolice() + " a été renouvelé automatiquement.\n\n"
                + "- Nouvelle période : du " + DATE.format(d.dateEffet()) + " au " + DATE.format(d.dateExpiration()) + "\n"
                + "- Prime TTC : " + montant(d.primeTtc()) + "\n\n"
                + "La facture de renouvellement est disponible dans votre espace client.\n\n"
                + "Cordialement,\nAUXIASSUR";
        envoyer(d.destinataire(), "AUXIASSUR - Votre contrat n° " + d.numPolice() + " a été renouvelé", corps);
    }

    /** Civilité, nom et prénom réunis ; les éléments absents sont simplement omis. */
    private static String identite(String civilite, String nom, String prenom) {
        StringBuilder b = new StringBuilder();
        if (civilite != null && !civilite.isBlank()) {
            b.append(civilite.trim());
        }
        appende(b, nom);
        appende(b, prenom);
        return b.toString().trim();
    }

    private static void appende(StringBuilder b, String morceau) {
        if (morceau != null && !morceau.isBlank()) {
            if (b.length() > 0) {
                b.append(' ');
            }
            b.append(morceau.trim());
        }
    }

    /** Montant en ariary avec séparateur de milliers français, ex. « 1 500 000 Ar ». */
    private static String montant(Double valeur) {
        return valeur == null ? "—" : String.format(Locale.FRANCE, "%,.0f Ar", valeur);
    }

    private void envoyer(String destinataire, String objet, String corps) {
        if (destinataire == null || destinataire.isBlank()) {
            log.warn("Mail non envoyé (aucun destinataire) : {}", objet);
            return;
        }
        if (!actif) {
            log.warn("[DEV] Mail désactivé. À {} — objet : {}\n{}", destinataire, objet, corps);
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
        message.setSubject(objet);
        message.setText(corps);
        try {
            sender.send(message);
            log.info("Mail envoyé à {} : {}", destinataire, objet);
        } catch (MailException e) {
            log.error("Envoi du mail à {} impossible : {}", destinataire, objet, e);
        }
    }
}
