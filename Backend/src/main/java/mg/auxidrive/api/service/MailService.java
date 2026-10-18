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

    /** Origine de la création du compte : fixe l'objet et l'accroche du mail d'identifiants. */
    public enum OrigineCompte { INSCRIPTION_VALIDEE, COMPTE_CREE }

    /** Données du mail d'identifiants, déjà extraites : l'envoi a lieu hors transaction, aucune entité n'y accède. */
    public record DonneesIdentifiants(String destinataire,
                                      String motDePasseTemporaire,
                                      String civilite, String nom, String prenom,
                                      String numerosAssure,
                                      OrigineCompte origine) {
    }

    private final String urlConnexion;
    private final String relais;

    public MailService(ObjectProvider<JavaMailSender> mailSender,
                       @Value("${app.mail.enabled:false}") boolean actif,
                       @Value("${spring.mail.username:}") String expediteur,
                       @Value("${app.url.connexion:http://localhost:4200/connexion}") String urlConnexion,
                       @Value("${app.mail.relay:${app.admin.email:}}") String relais) {
        this.mailSender = mailSender;
        this.actif = actif;
        this.expediteur = expediteur;
        this.urlConnexion = urlConnexion;
        this.relais = relais;
    }

    /**
     * Envoie les identifiants au client : accès personnalisés, lien de connexion, rappel du
     * changement de mot de passe et avertissement de confidentialité. L'objet et l'accroche
     * dépendent de l'origine du compte (inscription validée ou création par l'administrateur).
     * <p>
     * Si l'envoi échoue, les identifiants sont relayés à l'administrateur (app.mail.relay) afin
     * qu'il puisse les transmettre lui-même : le mot de passe en clair n'existe qu'à cet instant,
     * seul son hash est conservé en base, donc un envoi raté signifierait une perte définitive.
     * <p>
     * Méthode synchrone : l'appelant doit restituer le résultat à l'interface.
     * Tant que app.mail.enabled vaut false (développement), le contenu est affiché dans la console.
     *
     * @return true si le client a bien reçu ses identifiants
     */
    public boolean envoyerIdentifiants(DonneesIdentifiants d) {
        String accroche = d.origine() == OrigineCompte.INSCRIPTION_VALIDEE
                ? "Votre inscription sur AUXIDRIVE a été validée : votre compte client est ouvert."
                : "Votre compte client AUXIDRIVE a été créé à votre demande.";

        String identite = identite(d.civilite(), d.nom(), d.prenom());
        StringBuilder corps = new StringBuilder()
                .append(identite.isEmpty() ? "Bonjour," : "Bonjour " + identite + ",")
                .append("\n\n")
                .append(accroche).append('\n')
                .append("Vous pouvez dès à présent accéder à votre espace personnel.\n\n")
                .append("  Identifiant (e-mail)    : ").append(d.destinataire()).append('\n')
                .append("  Mot de passe temporaire : ").append(d.motDePasseTemporaire()).append('\n');
        if (aUnNumeroAssure(d)) {
            corps.append("  N° d'assuré             : ").append(d.numerosAssure().trim()).append('\n');
        }
        corps.append('\n')
                .append("Pour vous connecter :\n")
                .append("  1. Rendez-vous sur ").append(urlConnexion).append('\n')
                .append("  2. Saisissez votre identifiant et votre mot de passe\n")
                .append("  3. Changez votre mot de passe depuis le menu « Profil »\n")
                .append("     (8 caractères minimum)\n\n")
                .append("Ce mot de passe est personnel : ne le communiquez à personne et ne\n")
                .append("transférez pas ce message. Si vous n'êtes pas à l'origine de la création\n")
                .append("de ce compte, contactez-nous sans délai.\n\n")
                .append("Une question ? Répondez simplement à ce message.\n\n")
                .append("Cordialement,\nL'équipe AUXIDRIVE");

        String objet = d.origine() == OrigineCompte.INSCRIPTION_VALIDEE
                ? "AUXIDRIVE - Votre inscription est validée : vos identifiants"
                : "AUXIDRIVE - Votre compte client a été créé : vos identifiants";

        if (envoyer(d.destinataire(), objet, corps.toString())) {
            return true;
        }
        log.error("Identifiants de {} non transmis au client : tentative de relais vers {}",
                d.destinataire(), relais);
        envoyer(relais, "AUXIDRIVE - Identifiants non transmis à " + d.destinataire(),
                corpsRelais(d, identite));
        return false;
    }

    /** Corps du mail de secours adressé à l'administrateur quand l'envoi au client a échoué. */
    private static String corpsRelais(DonneesIdentifiants d, String identite) {
        StringBuilder corps = new StringBuilder()
                .append("L'envoi du mail d'identifiants à ").append(d.destinataire())
                .append(" a échoué.\n")
                .append("Transmettez les accès au client manuellement :\n\n")
                .append("  Client                  : ").append(identite.isEmpty() ? "—" : identite).append('\n')
                .append("  Identifiant (e-mail)    : ").append(d.destinataire()).append('\n')
                .append("  Mot de passe temporaire : ").append(d.motDePasseTemporaire()).append('\n');
        if (aUnNumeroAssure(d)) {
            corps.append("  N° d'assuré             : ").append(d.numerosAssure().trim()).append('\n');
        }
        corps.append("  Origine                 : ")
                .append(d.origine() == OrigineCompte.INSCRIPTION_VALIDEE
                        ? "inscription validée"
                        : "création par l'administrateur")
                .append('\n');
        return corps.toString();
    }

    private static boolean aUnNumeroAssure(DonneesIdentifiants d) {
        return d.numerosAssure() != null && !d.numerosAssure().isBlank();
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

    /**
     * Envoie un mail simple.
     *
     * @return false si l'envoi a réellement échoué ; en développement (app.mail.enabled=false),
     *         le contenu est journalisé et la méthode retourne true : l'absence d'envoi réel
     *         n'est pas une perte.
     */
    private boolean envoyer(String destinataire, String objet, String corps) {
        if (destinataire == null || destinataire.isBlank()) {
            log.warn("Mail non envoyé (aucun destinataire) : {}", objet);
            return false;
        }
        if (!actif) {
            log.warn("[DEV] Mail désactivé. À {} — objet : {}\n{}", destinataire, objet, corps);
            return true;
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
            return true;
        } catch (MailException e) {
            log.error("Envoi du mail à {} impossible : {}", destinataire, objet, e);
            return false;
        }
    }
}
