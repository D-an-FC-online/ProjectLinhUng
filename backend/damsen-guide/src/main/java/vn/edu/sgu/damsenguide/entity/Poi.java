package vn.edu.sgu.damsenguide.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "pois")
@Getter @Setter
public class Poi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "internal_name", nullable = false, length = 200)
    private String internalName;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "near_radius_m", nullable = false)
    private Integer nearRadiusM = 30;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "poi", fetch = FetchType.LAZY)
    private List<PoiContent> contents;
}
