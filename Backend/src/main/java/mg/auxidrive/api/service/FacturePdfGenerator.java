package mg.auxidrive.api.service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.ContratAuto;
import mg.auxidrive.api.model.Facture;
import mg.auxidrive.api.model.GarantieType;
import mg.auxidrive.api.model.Vehicule;

/**
 * Produit le PDF d'une facture de souscription.
 *
 * L'en-tête porte le logo de la marque et la mention de lieu et de date d'émission ; viennent ensuite
 * l'identité de l'assuré, les données du contrat (période de couverture, mode de renouvellement),
 * les véhicules couverts, les garanties incluses par la branche, puis le détail du calcul :
 * base taxable, TE, TACAVA, TVA et montant TTC. Le document s'achève sur une zone de signatures,
 * client à gauche, agent de production à droite.
 * L'assuré doit pouvoir vérifier chaque ligne, c'est la raison d'être des colonnes de détail.
 */
@Component
public class FacturePdfGenerator {

    private static final Logger log = LoggerFactory.getLogger(FacturePdfGenerator.class);

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DOSSIER = "factures";
    private static final String DEVISE = "Ar";

    /** Logo de la marque, partagé avec le front-end ; absent du classpath, la facture sort sans logo. */
    private static final String LOGO = "static/img/logo-auximad.png";
    private static final String LIEU = "Antananarivo";
    /** Largeur du logo en points ; la hauteur suit le ratio réel de l'image. */
    private static final float LARGEUR_LOGO = 75f;
    /** Hauteur réservée à la signature, en points. */
    private static final float HAUTEUR_SIGNATURE = 38f;

    /**
     * Écrit le PDF sur le disque et renvoie son chemin absolu.
     * Le nom du fichier reprend le numéro de facture (année + mois + séquence) pour être unique
     * et identifiable sans la base : un renouvellement n'écrase donc pas la facture précédente.
     *
     * Les véhicules et les garanties sont reçus en paramètre et non chargés ici : ce générateur
     * n'a ainsi aucune dépendance vers le service de facturation, ce qui évite une dépendance
     * circulaire entre les deux.
     */
    public String generer(Facture facture, List<ContratAuto> vehicules, List<GarantieType> garanties) {
        Contrat contrat = facture.getContrat();
        try {
            Path dossier = Paths.get(DOSSIER);
            Files.createDirectories(dossier);

            String nom = referenceFichier(facture, contrat) + ".pdf";
            Path chemin = dossier.resolve(nom).toAbsolutePath();

            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(chemin.toFile()));
            document.open();

            ecrireEntete(document, facture, contrat);
            ecrireAssure(document, contrat);
            ecrireContrat(document, contrat);
            ecrireVehicules(document, vehicules);
            ecrireGaranties(document, garanties);
            ecrireDetailCalcul(document, facture);
            ecrirePied(document, contrat);
            ecrireSignatures(document);

            document.close();
            return chemin.toString();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Impossible de générer le PDF de la facture " + facture.getIdFacture(), e);
        }
    }

    /**
     * En-tête : le logo de la marque et la mention de lieu et de date d'émission, alignés à gauche,
     * puis le titre et la référence centrés. Le logo est facultatif : s'il manque, la facture est
     * de toute façon produite, seule son en-tête est dépouillée.
     */
    private void ecrireEntete(Document document, Facture facture, Contrat contrat) {
        Image logo = chargerLogo();
        if (logo != null) {
            logo.setAlignment(Image.LEFT);
            logo.setSpacingAfter(2);
            document.add(logo);
        }
        if (facture.getDateEmission() != null) {
            Paragraph mention = new Paragraph(
                    "Fait le " + DATE_FR.format(facture.getDateEmission()) + " à " + LIEU, normal(9));
            mention.setAlignment(Element.ALIGN_LEFT);
            mention.setSpacingAfter(8);
            document.add(mention);
        }

        Paragraph titre = new Paragraph("FACTURE DE SOUSCRIPTION", gras(16));
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(4);
        document.add(titre);

        Paragraph ref = new Paragraph("Facture n° " + numeroFacture(facture)
                + (contrat.getNumPolice() == null ? "" : "  -  Police n° " + contrat.getNumPolice()),
                gras(11));
        ref.setAlignment(Element.ALIGN_CENTER);
        ref.setSpacingAfter(6);
        document.add(ref);
    }

    /**
     * Charge le logo depuis les ressources et le met à l'échelle voulue, la hauteur suivant le ratio
     * réel de l'image pour rester correcte si le fichier change. Toute défaillance est seulement
     * journalisée : le générateur ne doit jamais faire échouer la facture pour un logo manquant.
     *
     * @return l'image, ou null si elle n'a pas pu être chargée
     */
    private Image chargerLogo() {
        try (InputStream flux = new ClassPathResource(LOGO).getInputStream()) {
            Image image = Image.getInstance(flux.readAllBytes());
            image.scaleAbsolute(LARGEUR_LOGO, LARGEUR_LOGO * image.getHeight() / image.getWidth());
            return image;
        } catch (Exception e) {
            log.warn("Logo {} introuvable ou illisible : facture produite sans logo", LOGO, e);
            return null;
        }
    }

    /** Numéro de facture, ou à défaut l'identifiant technique, pour l'entête du PDF. */
    private String numeroFacture(Facture facture) {
        return facture.getNumFacture() == null || facture.getNumFacture().isBlank()
                ? String.valueOf(facture.getIdFacture())
                : facture.getNumFacture();
    }

    /** Nom de fichier : le numéro de facture, à défaut la police, à défaut l'identifiant du contrat. */
    private String referenceFichier(Facture facture, Contrat contrat) {
        if (facture.getNumFacture() != null && !facture.getNumFacture().isBlank()) {
            return facture.getNumFacture();
        }
        return contrat.getNumPolice() == null || contrat.getNumPolice().isBlank()
                ? "contrat-" + contrat.getIdContrat()
                : contrat.getNumPolice();
    }

    private void ecrireAssure(Document document, Contrat contrat) {
        Assure assure = contrat.getAssure();
        document.add(titreSection("Assuré"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);

        if (assure != null) {
            ligne(table, "Nom", assure.getNomAssure());
            ligne(table, "Prénom", assure.getPrenomAssure());
            ligne(table, "Numéro d'assuré", assure.getNumerosAssure());
            ligne(table, "Adresse", assure.getAdresse());
            ligne(table, "Contact", assure.getContact());
        }
        document.add(table);
    }

    /**
     * Données propres au contrat, séparées de l'identité de l'assuré : la période de couverture
     * est ce pour quoi l'assuré paie, elle mérite sa propre section plutôt que de figurer au
     * bas de la fiche assuré. Les lignes sans valeur sont simplement omises.
     */
    private void ecrireContrat(Document document, Contrat contrat) {
        document.add(titreSection("Contrat"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);

        if (contrat.getDateSouscription() != null) {
            ligne(table, "Date de souscription", contrat.getDateSouscription().format(DATE_FR));
        }
        if (contrat.getDateEffet() != null) {
            ligne(table, "Date d'effet", contrat.getDateEffet().format(DATE_FR));
        }
        if (contrat.getDateExpiration() != null) {
            ligne(table, "Date d'expiration", contrat.getDateExpiration().format(DATE_FR));
        }
        ligne(table, "Mode de renouvellement", contrat.getModeRenouvellement() == null
                ? "Non précisé" : contrat.getModeRenouvellement().getModeDeRnvltLib());
        document.add(table);
    }

    private void ecrireVehicules(Document document, List<ContratAuto> liens) {
        document.add(titreSection("Véhicules couverts"));
        if (liens.isEmpty()) {
            document.add(paragraphe("Aucun véhicule"));
            return;
        }

        PdfPTable table = tableEntete(new String[]{"Immatriculation", "Marque", "Modèle", "Énergie", "Puissance fiscale"});
        for (ContratAuto lien : liens) {
            Vehicule v = lien.getVehicule();
            if (v == null) {
                continue;
            }
            table.addCell(cellule(v.getNumImmatriculation()));
            table.addCell(cellule(v.getMarque()));
            table.addCell(cellule(v.getModele()));
            table.addCell(cellule(v.getEnergie()));
            table.addCell(cellule(v.getPuissanceFiscale() == null ? "" : v.getPuissanceFiscale() + " CV"));
        }
        document.add(table);
        document.add(espace());
    }

    private void ecrireGaranties(Document document, List<GarantieType> garanties) {
        document.add(titreSection("Garanties incluses"));
        if (garanties.isEmpty()) {
            document.add(paragraphe("Aucune garantie"));
            return;
        }
        StringBuilder ligne = new StringBuilder();
        for (GarantieType g : garanties) {
            if (ligne.length() > 0) {
                ligne.append("   -   ");
            }
            ligne.append(g.getCdGarantie()).append(" ").append(g.getLibelleGarantie());
        }
        document.add(paragraphe(ligne.toString()));
        document.add(espace());
    }

    /**
     * Détail du montant. La base taxable est la prime nette augmentée des accessoires, puis chaque
     * taxe est affichée sur sa propre ligne avant le total à payer.
     */
    private void ecrireDetailCalcul(Document document, Facture facture) {
        document.add(titreSection("Détail du calcul"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(70);
        table.setSpacingAfter(8);

        double accessoires = valeur(facture.getAccessoires());
        double base = valeur(facture.getPrimeNette());
        double netHorsAccessoires = base - accessoires;

        montant(table, "Prime nette", netHorsAccessoires);
        montant(table, "Accessoires", accessoires);
        montant(table, "Base taxable", base);
        if (facture.getMontantTe() != null && facture.getMontantTe() > 0) {
            montant(table, "TE", facture.getMontantTe());
        }
        if (facture.getMontantTacava() != null && facture.getMontantTacava() > 0) {
            montant(table, "TACAVA", facture.getMontantTacava());
        }
        if (facture.getMontantTva() != null && facture.getMontantTva() > 0) {
            montant(table, "TVA", facture.getMontantTva());
        }
        montant(table, "Total des taxes", valeur(facture.getMontantTaxes()));
        document.add(table);

        Paragraph total = new Paragraph("Montant TTC à payer : " + montant(facture.getMontantTtc()) + " " + DEVISE, gras(13));
        total.setSpacingBefore(6);
        document.add(total);
    }

    private void ecrirePied(Document document, Contrat contrat) {
        Paragraph mention = paragraphe("La commission d'intermédiaire est calculée en dehors du montant TTC "
                + "et n'est pas supportée par l'assuré."
                + (contrat.getMontantCommission() == null ? "" : " Montant de la commission : " + montant(contrat.getMontantCommission()) + " " + DEVISE + "."));
        mention.setSpacingBefore(10);
        document.add(mention);
    }

    /**
     * Zone de signatures, en tout bas de page : un trait vierge dans chaque colonne, sur lequel
     * l'assuré à gauche et l'agent de production à droite apposent leur signature, chacun sous
     * son libellé. Le trait est une bordure basse de cellule, la hauteur réservée laisse la place
     * d'écrire. Les noms ne sont pas pré-remplis : aucun n'est disponible au moment de l'émission.
     */
    private void ecrireSignatures(Document document) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(16);

        table.addCell(celluleLigneVierge());
        table.addCell(celluleLigneVierge());
        table.addCell(celluleSansBordure("Le client"));
        table.addCell(celluleSansBordure("L'agent de production"));

        document.add(table);
    }

    /** Colonne de la zone de signature : un simple trait, avec la hauteur d'un espace à signer. */
    private PdfPCell celluleLigneVierge() {
        PdfPCell cellule = new PdfPCell();
        cellule.setBorder(PdfPCell.BOTTOM);
        cellule.setBorderWidth(1f);
        cellule.setBorderColor(Color.BLACK);
        cellule.setMinimumHeight(HAUTEUR_SIGNATURE);
        cellule.setPadding(0);
        return cellule;
    }

    /** Légende sous un trait de signature. */
    private PdfPCell celluleSansBordure(String texte) {
        PdfPCell cellule = new PdfPCell(new Phrase(texte, normal(9)));
        cellule.setBorder(PdfPCell.NO_BORDER);
        cellule.setPaddingTop(3f);
        return cellule;
    }

    // ---- aides de mise en forme ----

    private Paragraph titreSection(String texte) {
        Paragraph p = new Paragraph(texte, gras(12));
        p.setSpacingBefore(6);
        p.setSpacingAfter(3);
        return p;
    }

    private Paragraph paragraphe(String texte) {
        return new Paragraph(texte == null ? "" : texte);
    }

    private Paragraph espace() {
        Paragraph p = new Paragraph();
        p.setSpacingAfter(4);
        return p;
    }

    private Font gras(float taille) {
        return new Font(Font.HELVETICA, taille, Font.BOLD);
    }

    private Font normal(float taille) {
        return new Font(Font.HELVETICA, taille);
    }

    private void ligne(PdfPTable table, String libelle, String valeur) {
        table.addCell(cellule(libelle));
        table.addCell(cellule(valeur));
    }

    private void montant(PdfPTable table, String libelle, double montant) {
        PdfPCell c1 = cellule(libelle);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);
        table.addCell(cellule(montant(montant)));
    }

    private PdfPTable tableEntete(String[] entetes) {
        PdfPTable table = new PdfPTable(entetes.length);
        table.setWidthPercentage(100);
        for (String e : entetes) {
            PdfPCell c = cellule(e);
            c.setBackgroundColor(new java.awt.Color(230, 230, 230));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c);
        }
        return table;
    }

    private PdfPCell cellule(String texte) {
        PdfPCell c = new PdfPCell(new Phrase(texte == null ? "" : texte));
        // lignes serrées : la facture doit tenir sur une page dans le cas courant,
        // un interligne aéré coûte une vingtaine de points par section
        c.setPaddingTop(1f);
        c.setPaddingBottom(1f);
        return c;
    }

    private String montant(double valeur) {
        return new BigDecimal(valeur).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private double valeur(Double v) {
        return v == null ? 0d : v;
    }
}