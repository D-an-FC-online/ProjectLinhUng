package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @Column(name = "name_vi") private String nameVi;
    @Column(name = "name_en") private String nameEn;
    @Column(name = "name_ko") private String nameKo;
    @Column(name = "is_active") private boolean active = true;

    /** Tên theo ngôn ngữ, thiếu thì lấy tiếng Việt. */
    public String nameFor(String lang) {
        String n = switch (lang == null ? "vi" : lang) {
            case "en" -> nameEn;
            case "ko" -> nameKo;
            default -> nameVi;
        };
        return (n == null || n.isBlank()) ? nameVi : n;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameKo() { return nameKo; }
    public void setNameKo(String nameKo) { this.nameKo = nameKo; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
