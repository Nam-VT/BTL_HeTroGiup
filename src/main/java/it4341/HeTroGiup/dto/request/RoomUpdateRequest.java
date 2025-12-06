package it4341.HeTroGiup.dto.request;

import it4341.HeTroGiup.Enum.RoomStatus;
import it4341.HeTroGiup.Enum.RoomType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomUpdateRequest {
    private Long areaTypeId;

    private String title;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long priceVnd;
    private BigDecimal areaSqm;
    private RoomType roomType;
    private RoomStatus status;

    private Long roomCoverImageId;
    private List<Long> roomNotCoverImageIds;

    private List<RoomSurveyAnswerRequest> surveyAnswers;
}
