package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.request.*;
import it4341.HeTroGiup.dto.response.*;
import it4341.HeTroGiup.service.RoomService;
import it4341.HeTroGiup.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoutingService routingService;

    // 1. Thêm mới phòng
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createRoom(@RequestBody RoomCreateRequest request) {
        RoomCreateResponse result = roomService.createRoom(request);

        return ResponseEntity.ok(new ApiResponse("00", null, result));
    }

    // 2. Sửa thông tin phòng
    @PostMapping("/update")
    public ResponseEntity<ApiResponse> updateRoom(
            @RequestBody RoomUpdateRequest request) {
        RoomUpdateResponse result = roomService.updateRoom(request);
        return ResponseEntity.ok(new ApiResponse("00", null, result));
    }

    // 3. Xem danh sách phòng (Theo chủ trọ)
    @PostMapping("/all")
    public ResponseEntity<ApiResponse> getAllRooms(@RequestBody RoomFilterRequest request) {
        try {
            // Service giờ trả về RoomFilterResponse
            RoomFilterResponse result = roomService.getAllRooms(request);
            return ResponseEntity.ok(new ApiResponse("00", null, result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteRooms(@RequestBody RoomDeleteRequest request) {
        try {
            roomService.deleteRooms(request);
            return ResponseEntity.ok(new ApiResponse("00", null, "Xóa danh sách phòng thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }

    @PostMapping("/save-distance")
    public  ResponseEntity<ApiResponse> saveDistanceFromRoomToScholl(@RequestBody RoomToSchoolRequest roomToSchoolRequest){
        try {
            routingService.findShortestRoutes(roomToSchoolRequest);
            return ResponseEntity.ok(new ApiResponse("00", null, "successfull"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }

    @PostMapping("/view-map")
    public ResponseEntity<ApiResponse> getRoutePath(@RequestBody RouteRequest request) {
        try {
            RouteResponse result = routingService.getRouteDetail(request);
            return ResponseEntity.ok(new ApiResponse("00", null, result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }

}