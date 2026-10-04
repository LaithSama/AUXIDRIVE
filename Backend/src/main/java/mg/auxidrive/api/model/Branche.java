package mg.auxidrive.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "branches")
public class Branche {

    @Id
    @Column(name = "cdBranche", length = 2)
    private String cdBranche;

    @Column(name = "branches", length = 100)
    private String branches;

    @Column(name = "cdAstype", length = 2)
    private String cdAstype;

    @Column(name = "cdGdbranche", length = 2)
    private String cdGdbranche;

    @Column(name = "lblTacava", length = 5)
    private String lblTacava;

    @Column(name = "cdAsattest", length = 2)
    private String cdAsattest;

    @Column(name = "libAs", length = 50)
    private String libAs;

    public Branche(String branches, String cdAstype, String cdGdbranche, String lblTacava, String cdAsattest, String libAs) {
        this.branches = branches;
        this.cdAstype = cdAstype;
        this.cdGdbranche = cdGdbranche;
        this.lblTacava = lblTacava;
        this.cdAsattest = cdAsattest;
        this.libAs = libAs;
    }

    public Branche(String cdBranche, String branches, String cdAstype, String cdGdbranche, String lblTacava, String cdAsattest, String libAs) {
        this.cdBranche = cdBranche;
        this.branches = branches;
        this.cdAstype = cdAstype;
        this.cdGdbranche = cdGdbranche;
        this.lblTacava = lblTacava;
        this.cdAsattest = cdAsattest;
        this.libAs = libAs;
    }

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
