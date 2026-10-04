package mg.auxidrive.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "avenantsType")
public class AvenantType {

    @Id
    @Column(name = "natureAvenantCode", length = 5)
    private String natureAvenantCode;

    @Column(name = "natureAvenantNum")
    private Integer natureAvenantNum;

    @Column(name = "natureAvenant", length = 50)
    private String natureAvenant;

    public AvenantType(Integer natureAvenantNum, String natureAvenant) {
        this.natureAvenantNum = natureAvenantNum;
        this.natureAvenant = natureAvenant;
    }

    public AvenantType(String natureAvenantCode, Integer natureAvenantNum, String natureAvenant) {
        this.natureAvenantCode = natureAvenantCode;
        this.natureAvenantNum = natureAvenantNum;
        this.natureAvenant = natureAvenant;
    }

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
