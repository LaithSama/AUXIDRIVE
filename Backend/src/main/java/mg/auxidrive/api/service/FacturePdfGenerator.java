package mg.auxidrive.api.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
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
 * Le document reprend l'identification du contrat, les véhicules couverts, les garanties incluses
 * par la branche, puis le détail du calcul : base taxable, TE, TACAVA, TVA et montant TTC.
 * L'assuré doit pouvoir vérifier chaque ligne, c'est la raison d'être des colonnes de détail.
 */
@Component
public class FacturePdfGenerator {

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DOSSIER = "factures";
    private static final String DEVISE = "Ar";

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
            ecrireVehicules(document, vehicules);
            ecrireGaranties(document, garanties);
            ecrireDetailCalcul(document, facture);
            ecrirePied(document, contrat);

            document.close();
            return chemin.toString();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Impossible de générer le PDF de la facture " + facture.getIdFacture(), e);
        }
    }

    private void ecrireEntete(Document document, Facture facture, Contrat contrat) {
        Paragraph titre = new Paragraph("FACTURE DE SOUSCRIPTION", gras(16));
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(4);
        document.add(titre);

        Paragraph ref = new Paragraph("Facture n° " + numeroFacture(facture)
                + (contrat.getNumPolice() == null ? "" : "  -  Police n° " + contrat.getNumPolice()),
                gras(11));
        ref.setAlignment(Element.ALIGN_CENTER);
        ref.setSpacingAfter(14);
        document.add(ref);
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
        table.setSpacingAfter(12);

        if (assure != null) {
            ligne(table, "Nom", assure.getNomAssure());
            ligne(table, "Prénom", assure.getPrenomAssure());
            ligne(table, "Numéro d'assuré", assure.getNumerosAssure());
            ligne(table, "Adresse", assure.getAdresse());
            ligne(table, "Contact", assure.getContact());
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
        table.setSpacingBefore(6);
        table.setSpacingAfter(12);

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

        Paragraph total = new Paragraph("Montant TTC à payer : " + montant(facture.getMontantTtc()) + " " + DEVISE, gras(13));
        total.setSpacingBefore(6);
        document.add(total);
    }

    private void ecrirePied(Document document, Contrat contrat) {
        Paragraph mention = paragraphe("La commission d'intermédiaire est calculée en dehors du montant TTC "
                + "et n'est pas supportée par l'assuré."
                + (contrat.getMontantCommission() == null ? "" : " Montant de la commission : " + montant(contrat.getMontantCommission()) + " " + DEVISE + "."));
        mention.setSpacingBefore(16);
        document.add(mention);
    }

    // ---- aides de mise en forme ----

    private Paragraph titreSection(String texte) {
        Paragraph p = new Paragraph(texte, gras(12));
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph paragraphe(String texte) {
        return new Paragraph(texte == null ? "" : texte);
    }

    private Paragraph espace() {
        Paragraph p = new Paragraph();
        p.setSpacingAfter(6);
        return p;
    }

    private Font gras(float taille) {
        return new Font(Font.HELVETICA, taille, Font.BOLD);
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
        return new PdfPCell(new Phrase(texte == null ? "" : texte));
    }

    private String montant(double valeur) {
        return new BigDecimal(valeur).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private double valeur(Double v) {
        return v == null ? 0d : v;
    }
}