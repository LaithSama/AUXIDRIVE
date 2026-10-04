package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Branches")
public class Branche {

    @Id
    @Column(name = "cd_branche", length = 2)
    private String cdBranche;

    @Column(name = "branches", length = 100)
    private String branches;

    @Column(name = "cd_astype", length = 2)
    private String cdAstype;

    @Column(name = "cd_gdbranche", length = 2)
    private String cdGdbranche;

    @Column(name = "lbl_tacava", length = 5)
    private String lblTacava;

    @Column(name = "cd_asattest", length = 2)
    private String cdAsattest;

    @Column(name = "lib_as", length = 50)
    private String libAs;

    public String getCdBranche() {
        return cdBranche;
    }

    public void setCdBranche(String cdBranche) {
        this.cdBranche = cdBranche;
    }

    public String getBranches() {
        return branches;
    }

    public void setBranches(String branches) {
        this.branches = branches;
    }

    public String getCdAstype() {
        return cdAstype;
    }

    public void setCdAstype(String cdAstype) {
        this.cdAstype = cdAstype;
    }

    public String getCdGdbranche() {
        return cdGdbranche;
    }

    public void setCdGdbranche(String cdGdbranche) {
        this.cdGdbranche = cdGdbranche;
    }

    public String getLblTacava() {
        return lblTacava;
    }

    public void setLblTacava(String lblTacava) {
        this.lblTacava = lblTacava;
    }

    public String getCdAsattest() {
        return cdAsattest;
    }

    public void setCdAsattest(String cdAsattest) {
        this.cdAsattest = cdAsattest;
    }

    public String getLibAs() {
        return libAs;
    }

    public void setLibAs(String libAs) {
        this.libAs = libAs;
    }
}
