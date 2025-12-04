package it4341.HeTroGiup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_order")
    private Integer questionOrder;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
