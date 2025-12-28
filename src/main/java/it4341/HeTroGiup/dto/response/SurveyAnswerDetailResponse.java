package it4341.HeTroGiup.dto.response;

import it4341.HeTroGiup.Enum.SurveyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerDetailResponse {
    private Long id;
    private Long roomId;
    private Long surveyQuestionId;
    private Long surveyId;
    private SurveyType surveyType;
    private String surveyQuestionText;
    private Integer surveyQuestionOrder;
    private Integer point;
}