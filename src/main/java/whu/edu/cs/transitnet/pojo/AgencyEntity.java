package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Ria
 */
@Entity
@Table(name = "agency")
public class AgencyEntity {
    private String agencyId;
    private String agencyName;
    private String agencyUrl;
    private String agencyTimezone;
    private String agencyLang;
    private String agencyPhone;

    @Id
    @Column(name = "agency_id")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    @Basic
    @Column(name = "agency_name")
    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    @Basic
    @Column(name = "agency_url")
    public String getAgencyUrl() {
        return agencyUrl;
    }

    public void setAgencyUrl(String agencyUrl) {
        this.agencyUrl = agencyUrl;
    }

    @Basic
    @Column(name = "agency_timezone")
    public String getAgencyTimezone() {
        return agencyTimezone;
    }

    public void setAgencyTimezone(String agencyTimezone) {
        this.agencyTimezone = agencyTimezone;
    }

    @Basic
    @Column(name = "agency_lang")
    public String getAgencyLang() {
        return agencyLang;
    }

    public void setAgencyLang(String agencyLang) {
        this.agencyLang = agencyLang;
    }

    @Basic
    @Column(name = "agency_phone")
    public String getAgencyPhone() {
        return agencyPhone;
    }

    public void setAgencyPhone(String agencyPhone) {
        this.agencyPhone = agencyPhone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgencyEntity that = (AgencyEntity) o;
        return Objects.equals(agencyId, that.agencyId) &&
                Objects.equals(agencyName, that.agencyName) &&
                Objects.equals(agencyUrl, that.agencyUrl) &&
                Objects.equals(agencyTimezone, that.agencyTimezone) &&
                Objects.equals(agencyLang, that.agencyLang) &&
                Objects.equals(agencyPhone, that.agencyPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agencyId, agencyName, agencyUrl, agencyTimezone, agencyLang, agencyPhone);
    }
}
