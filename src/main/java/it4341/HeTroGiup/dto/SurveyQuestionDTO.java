package it4341.HeTroGiup.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SurveyQuestionDTO {
    private Long id;
    private Long surveyId;
    private String questionText;
    private Integer questionOrder;
}