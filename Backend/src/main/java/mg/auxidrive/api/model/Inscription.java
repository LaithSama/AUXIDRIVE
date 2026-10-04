package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inscriptions")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscription")
    private Long idInscription;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idAssure", unique = true)
    private Assure assure;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutInscription")
    private StatutInscription statutInscription;

    @Column(name = "dateDemande")
    private LocalDateTime dateDemande;

    @Column(name = "dateDecision")
    private LocalDateTime dateDecision;

    @Column(name = "motifRefus", length = 255)
    private String motifRefus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtil")
    private Utilisateur agent;
}
