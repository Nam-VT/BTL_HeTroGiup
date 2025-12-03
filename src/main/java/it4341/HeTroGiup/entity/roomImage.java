package it4341.HeTroGiup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class roomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attach_file_id", nullable = false)
    private attachFile attachFile;

    @Column(name = "is_cover")
    private Boolean isCover = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}