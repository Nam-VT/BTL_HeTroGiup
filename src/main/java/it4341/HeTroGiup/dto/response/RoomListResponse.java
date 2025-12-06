package it4341.HeTroGiup.dto.response;

import it4341.HeTroGiup.Enum.RoomStatus;
import it4341.HeTroGiup.Enum.RoomType;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomListResponse {
    private Long id;
    private Long landlordUserId;
    private Long areaTypeId;

    private String areaTypeName;

    private String title;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long priceVnd;
    private BigDecimal areaSqm;
    private RoomType roomType;
    private RoomStatus status;
    private BigDecimal avgAmenity;
    private BigDecimal avgSecurity;

    private Long roomCoverImageId;
    private String roomCoverImageUrl;
}