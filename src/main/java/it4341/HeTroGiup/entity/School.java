package it4341.HeTroGiup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String schoolName;

    @Column(name = "name_search")
    private String nameSearch;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}
