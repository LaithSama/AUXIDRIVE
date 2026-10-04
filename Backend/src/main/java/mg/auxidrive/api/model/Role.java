package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRole")
    private Long idRole;

    @Column(name = "libRole", length = 30)
    private String libRole;

    @Column(name = "description", length = 255)
    private String description;

    public Role(String libRole, String description) {
        this.libRole = libRole;
        this.description = description;
    }

    public Role(Long idRole, String libRole, String description) {
        this.idRole = idRole;
        this.libRole = libRole;
        this.description = description;
    }

    public Role() {

    }

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
