package it4341.HeTroGiup.entity;

import it4341.HeTroGiup.Enum.RoomType;
import it4341.HeTroGiup.Enum.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_user_id", nullable = false)
    private Landlord landlord;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String address;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "price_vnd")
    private Long priceVnd;

    @Column(name = "area_sqm", precision = 8, scale = 2)
    private BigDecimal areaSqm;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status = RoomStatus.PENDING;

    @Column(name = "avg_amenity", precision = 2, scale = 1)
    private BigDecimal avgAmenity;

    @Column(name = "avg_scurity", precision = 2, scale = 1)
    private BigDecimal avgScurity;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "room")
    private List<RoomImage> images;
}
