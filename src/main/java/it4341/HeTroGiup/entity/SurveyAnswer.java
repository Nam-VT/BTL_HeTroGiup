package it4341.HeTroGiup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_id", nullable = false)
    private SurveyQuestion surveyQuestion;

    private Integer point;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
