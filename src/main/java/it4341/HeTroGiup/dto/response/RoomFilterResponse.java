package it4341.HeTroGiup.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RoomFilterResponse {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    private List<Long> rowIdsInMatrix;
    private List<List<Double>> initMatrix;

    private List<RoomListResponse> data;
}