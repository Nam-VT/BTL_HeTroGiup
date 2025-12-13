package it4341.HeTroGiup.dto.request;

import it4341.HeTroGiup.Enum.RoomType;
import lombok.Data;

import java.util.List;

@Data
public class RoomFilterRequest {
    private int pageNumber;
    private int pageSize;

    private Long schoolId;

    private Long fromPrice;
    private Long toPrice;

    private Double fromDistance;
    private Double toDistance;

    private Double fromArea;
    private Double toArea;

    private Double fromSecurityPoints;
    private Double toSecurityPoints;

    private Double fromAmenityPoints;
    private Double toAmenityPoints;

    private List<Long> areaTypeIds;

    private List<RoomType> roomTypes;
}