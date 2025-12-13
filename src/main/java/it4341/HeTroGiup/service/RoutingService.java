package it4341.HeTroGiup.service;

import it4341.HeTroGiup.dto.request.RoomToSchoolRequest;
import it4341.HeTroGiup.dto.request.RouteRequest;
import it4341.HeTroGiup.dto.response.BoardingRoomResponse;
import it4341.HeTroGiup.dto.response.DistanceResponse;
import it4341.HeTroGiup.dto.response.RouteResponse;
import it4341.HeTroGiup.entity.Room;
import it4341.HeTroGiup.entity.RoomSchool;
import it4341.HeTroGiup.entity.School;
import it4341.HeTroGiup.repository.RoomRepository;
import it4341.HeTroGiup.repository.RoomSchoolRepository;
import it4341.HeTroGiup.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final RoomRepository roomRepository;
    private final SchoolRepository schoolRepository;
    private final RoomSchoolRepository roomSchoolRepository;
    private final GeocodingService geocodingService;

    @Value("${ors.api-key}")
    private String apiKey;

    private final WebClient client = WebClient.create("https://api.openrouteservice.org");

    public List<DistanceResponse> findShortestRoutes(RoomToSchoolRequest roomToSchoolRequest) {
        List<DistanceResponse> result = new ArrayList<>();

        // 1. Lấy thông tin School
        School school = schoolRepository.findById(roomToSchoolRequest.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        double schoolLat = 0;
        double schoolLng = 0;

        // Cố gắng lấy tọa độ trường
        try {
            // Lưu ý: Nếu tên trường trong DB là "DAI HOC BACH KHOA", Nominatim có thể không tìm thấy
            // nếu không có từ khóa "Ha Noi" hoặc "Vietnam".
            // Tốt nhất nên nối thêm địa danh: getCoordinates(school.getName() + " Ha Noi")
            double[] geo = geocodingService.getCoordinates(school.getNameSearch());
            schoolLat = geo[0];
            schoolLng = geo[1];
        } catch (Exception e) {
            System.err.println("Geocoding failed for school: " + school.getNameSearch());
            // Giữ nguyên 0,0
        }

        // 2. Lấy danh sách phòng
        List<BoardingRoomResponse> allRooms = getAllBoardingRoom();
        if (allRooms.isEmpty()) return result;

        // 3. Check Cache
        List<RoomSchool> cachedList = roomSchoolRepository.findBySchoolId(school.getId());
        Map<Long, RoomSchool> cacheMap = cachedList.stream()
                .collect(Collectors.toMap(RoomSchool::getRoomId, Function.identity()));

        List<RoomSchool> newRecordsToSave = new ArrayList<>();

        // 4. Duyệt và Xử lý
        for (BoardingRoomResponse room : allRooms) {
            double finalDistance;
            List<List<Double>> finalGeometry;

            // Kiểm tra Cache
            if (cacheMap.containsKey(room.getId())) {
                RoomSchool cachedRecord = cacheMap.get(room.getId());
                finalDistance = cachedRecord.getDistance();

                // Tạo đường thẳng [Phòng -> Trường] nếu có tọa độ
                if (room.getLat() != null && room.getLng() != null && schoolLat != 0) {
                    finalGeometry = List.of(
                            List.of(room.getLng(), room.getLat()),
                            List.of(schoolLng, schoolLat)
                    );
                } else {
                    finalGeometry = Collections.emptyList();
                }
            } else {
                // Chưa có Cache -> Cần gọi API
                Double roomLat = room.getLat();
                Double roomLng = room.getLng();

                // --- QUAN TRỌNG: CHẶN GỌI API NẾU TỌA ĐỘ BẰNG 0 ---
                if (roomLat != null && roomLng != null && schoolLat != 0 && schoolLng != 0) {

                    // Gọi API (đã sửa hàm getDistance bên dưới)
                    RouteData routeData = getDistance(roomLat, roomLng, schoolLat, schoolLng);

                    finalDistance = routeData.getDistanceKm();
                    finalGeometry = routeData.getCoordinates();

                    // Lưu Cache
                    RoomSchool newRecord = RoomSchool.builder()
                            .roomId(room.getId())
                            .schoolId(school.getId())
                            .distance(finalDistance)
                            .isDeleted(false)
                            .build();
                    newRecordsToSave.add(newRecord);
                } else {
                    // Nếu không có tọa độ hợp lệ, gán mặc định để không lỗi
                    finalDistance = 9999.0;
                    finalGeometry = Collections.emptyList();
                }
            }

            result.add(DistanceResponse.builder()
                    .roomId(room.getId())
                    .roomName(room.getName())
                    .address(room.getAddress())
                    .distanceKm(finalDistance)
                    .geometry(finalGeometry)
                    .build());
        }

        // 5. Lưu Cache
        if (!newRecordsToSave.isEmpty()) {
            roomSchoolRepository.saveAll(newRecordsToSave);
        }

        result.sort(Comparator.comparing(DistanceResponse::getDistanceKm));
        return result;
    }

    public List<BoardingRoomResponse> getAllBoardingRoom() {
        List<Room> rooms = roomRepository.findByIsDeletedFalse();
        return rooms.stream().map(room -> BoardingRoomResponse.builder()
                .id(room.getId())
                .name(room.getTitle())
                .address(room.getAddress())
                .lat(room.getLatitude() != null ? room.getLatitude().doubleValue() : null)
                .lng(room.getLongitude() != null ? room.getLongitude().doubleValue() : null)
                .build()).collect(Collectors.toList());
    }

    public void updateAllDistancesForRoom(Room room) {
        if (room.getLatitude() == null || room.getLongitude() == null) return;

        List<RoomSchool> cacheList = roomSchoolRepository.findByRoomId(room.getId());
        if (cacheList.isEmpty()) return; // Chưa từng tính khoảng cách thì thôi

        List<Long> schoolIds = cacheList.stream().map(RoomSchool::getSchoolId).collect(Collectors.toList());
        List<School> schools = schoolRepository.findAllById(schoolIds);
        Map<Long, School> schoolMap = schools.stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        List<RoomSchool> toUpdate = new ArrayList<>();

        for (RoomSchool rs : cacheList) {
            School school = schoolMap.get(rs.getSchoolId());
            if (school == null) continue;

            double schoolLat, schoolLng;
            try {
                double[] geo = geocodingService.getCoordinates(school.getNameSearch());
                schoolLat = geo[0];
                schoolLng = geo[1];
            } catch (Exception e) {
                continue;
            }

            RouteData routeData = getDistance(
                    room.getLatitude().doubleValue(),
                    room.getLongitude().doubleValue(),
                    schoolLat,
                    schoolLng
            );

            rs.setDistance(routeData.getDistanceKm());
            toUpdate.add(rs);
        }

        if (!toUpdate.isEmpty()) {
            roomSchoolRepository.saveAll(toUpdate);
        }
    }

    public RouteResponse getRouteDetail(RouteRequest req) {
        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        School school = schoolRepository.findById(req.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Trường không tồn tại"));

        if (room.getLatitude() == null || room.getLongitude() == null) {
            throw new RuntimeException("Phòng chưa có tọa độ");
        }

        double schoolLat = 0, schoolLng = 0;
        try {
            double[] geo = geocodingService.getCoordinates(school.getNameSearch());
            schoolLat = geo[0];
            schoolLng = geo[1];
        } catch (Exception e) {
            return new RouteResponse(0.0, Collections.emptyList());
        }

        RouteData routeData = getDistance(
                room.getLatitude().doubleValue(),
                room.getLongitude().doubleValue(),
                schoolLat,
                schoolLng
        );

        return RouteResponse.builder()
                .distance(routeData.getDistanceKm())
                .geometry(routeData.getCoordinates())
                .build();
    }


    private RouteData getDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        try {
            // Dùng HashMap và ArrayList tường minh để tránh lỗi serialization
            Map<String, Object> body = new HashMap<>();
            List<List<Double>> coordinates = new ArrayList<>();

            // OpenRouteService yêu cầu: [Longitude, Latitude]
            coordinates.add(Arrays.asList(lng1, lat1)); // Điểm xuất phát (Phòng)
            coordinates.add(Arrays.asList(lng2, lat2)); // Điểm đến (Trường)

            body.put("coordinates", coordinates);

            // In ra console để kiểm tra xem tọa độ gửi đi là gì
            System.out.println("Calling ORS with coords: " + coordinates);

            Map response = client.post()
                    .uri("/v2/directions/driving-car")
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return new RouteData(9999.0, Collections.emptyList());

            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
            if (routes == null || routes.isEmpty()) return new RouteData(9999.0, Collections.emptyList());

            Map<String, Object> route = routes.get(0);
            Map<String, Object> summary = (Map<String, Object>) route.get("summary");

            double meters = 9999000.0;
            if (summary != null && summary.get("distance") != null) {
                meters = ((Number) summary.get("distance")).doubleValue();
            }

            Object geometryObj = route.get("geometry");
            List<List<Double>> decodedCoords = decodeGeometry(geometryObj);

            return new RouteData(meters / 1000.0, decodedCoords);

        } catch (Exception e) {
            // In lỗi chi tiết ra console
            System.err.println("ORS API Error: " + e.getMessage());
            return new RouteData(9999.0, Collections.emptyList());
        }
    }

    private List<List<Double>> decodeGeometry(Object geometryObj) {
        if (geometryObj == null) return Collections.emptyList();
        if (geometryObj instanceof Map) {
            Map<String, Object> geo = (Map<String, Object>) geometryObj;
            if (geo.get("coordinates") instanceof List) {
                return (List<List<Double>>) geo.get("coordinates");
            }
        }
        if (geometryObj instanceof String) {
            return decodePolylineToCoordinates((String) geometryObj);
        }
        return Collections.emptyList();
    }

    private List<List<Double>> decodePolylineToCoordinates(String encoded) {
        List<List<Double>> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double finalLat = lat / 1E5;
            double finalLng = lng / 1E5;
            poly.add(List.of(finalLng, finalLat));
        }
        return poly;
    }

    private static class RouteData {
        private final double distanceKm;
        private final List<List<Double>> coordinates;

        RouteData(double distanceKm, List<List<Double>> coordinates) {
            this.distanceKm = distanceKm;
            this.coordinates = coordinates;
        }

        public double getDistanceKm() { return distanceKm; }
        public List<List<Double>> getCoordinates() { return coordinates; }
    }
}