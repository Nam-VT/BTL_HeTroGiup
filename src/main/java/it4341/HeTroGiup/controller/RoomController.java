package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.request.RoomCreateRequest;
import it4341.HeTroGiup.dto.request.RoomDeleteRequest;
import it4341.HeTroGiup.dto.request.RoomPageRequest;
import it4341.HeTroGiup.dto.request.RoomUpdateRequest;
import it4341.HeTroGiup.dto.response.*;
import it4341.HeTroGiup.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 1. Thêm mới phòng
    @PostMapping
    public ResponseEntity<ApiResponse> createRoom(@RequestBody RoomCreateRequest request) {
        RoomCreateResponse result = roomService.createRoom(request);

        return ResponseEntity.ok(new ApiResponse("00", null, result));
    }

    // 2. Sửa thông tin phòng
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse> updateRoom(
            @RequestBody RoomUpdateRequest request) {
        RoomUpdateResponse result = roomService.updateRoom(request);
        return ResponseEntity.ok(new ApiResponse("00", null, result));
    }

    // 3. Xem danh sách phòng (Theo chủ trọ)
    @PostMapping("/all")
    public ResponseEntity<ApiResponse> getAllRooms(@RequestBody RoomPageRequest request) {
        try {
            PageResponse<RoomListResponse> result = roomService.getAllRooms(request);
            return ResponseEntity.ok(new ApiResponse("00", null, result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }

    // 4. Xem chi tiết phòng
//    @GetMapping("/{id}")
//    public ResponseEntity<RoomDTO> getRoomDetail(@PathVariable Long id) {
//        return ResponseEntity.ok(roomService.getRoomDetail(id));
//    }

    @PostMapping()
    public ResponseEntity<ApiResponse> deleteRoom(@RequestBody RoomDeleteRequest req) {
        try {
            roomService.deleteRoom(req);
            return ResponseEntity.ok(new ApiResponse("00", null, "Xóa phòng thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }
}