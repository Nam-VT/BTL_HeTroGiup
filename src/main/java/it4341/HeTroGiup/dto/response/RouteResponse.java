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
public class RouteResponse {
    private Double distance; // Đơn vị: Km (hoặc mét tùy bạn chỉnh)
    private List<List<Double>> geometry; // Cấu trúc [[lng, lat], [lng, lat], ...]
}
