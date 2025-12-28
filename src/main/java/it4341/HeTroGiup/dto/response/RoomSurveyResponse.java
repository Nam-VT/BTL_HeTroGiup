package it4341.HeTroGiup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSurveyResponse {
    private Long roomId;
    private List<SurveyAnswerDetailResponse> surveyAnswers;
}