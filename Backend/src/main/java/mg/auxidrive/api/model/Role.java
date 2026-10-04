package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    @Column(name = "lib_role", length = 30)
    private String libRole;

    @Column(name = "description", length = 255)
    private String description;

    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public String getLibRole() {
        return libRole;
    }

    public void setLibRole(String libRole) {
        this.libRole = libRole;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
