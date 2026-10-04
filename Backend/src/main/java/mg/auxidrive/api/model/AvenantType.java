package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Avenants_type")
public class AvenantType {

    @Id
    @Column(name = "nature_avenant_code", length = 5)
    private String natureAvenantCode;

    @Column(name = "nature_avenant_num")
    private Integer natureAvenantNum;

    @Column(name = "nature_avenant", length = 50)
    private String natureAvenant;

    public String getNatureAvenantCode() {
        return natureAvenantCode;
    }

    public void setNatureAvenantCode(String natureAvenantCode) {
        this.natureAvenantCode = natureAvenantCode;
    }

    public Integer getNatureAvenantNum() {
        return natureAvenantNum;
    }

    public void setNatureAvenantNum(Integer natureAvenantNum) {
        this.natureAvenantNum = natureAvenantNum;
    }

    public String getNatureAvenant() {
        return natureAvenant;
    }

    public void setNatureAvenant(String natureAvenant) {
        this.natureAvenant = natureAvenant;
    }
}
