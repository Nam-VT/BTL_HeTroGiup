package it4341.HeTroGiup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSchool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "school_id")
    private Long schoolId;

    private double distance;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}