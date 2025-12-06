package it4341.HeTroGiup.dto.request;

import lombok.Data;

@Data
public class RoomSurveyAnswerRequest {
    private Long surveyQuestionId;
    private Integer point;
}
