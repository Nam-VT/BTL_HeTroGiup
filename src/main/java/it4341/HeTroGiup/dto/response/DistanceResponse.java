package it4341.HeTroGiup.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistanceResponse {
    private Long roomId;
    private String roomName;
    private String address;
    private Double distanceKm;
    private List<List<Double>> geometry; // [ [lng, lat], [lng, lat], ... ]
}