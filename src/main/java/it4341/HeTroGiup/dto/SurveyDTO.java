package it4341.HeTroGiup.dto;

import it4341.HeTroGiup.Enum.SurveyType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SurveyDTO {
    private Long id;
    private SurveyType type;
    private String title;
    private String description;
}