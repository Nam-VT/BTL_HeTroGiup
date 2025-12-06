package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.request.RoomCreateRequest;
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
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomUpdateRequest request) { // Đã đổi thành RoomCreateRequest
        RoomUpdateResponse result = roomService.updateRoom(id, request);
        return ResponseEntity.ok(new ApiResponse("00", null, result));
    }

    // 3. Xem danh sách phòng (Theo chủ trọ)
    @PostMapping("/all")
    public ResponseEntity<ApiResponse> getMyRooms(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam Long id
    ) {

        PageResponse<RoomListResponse> result = roomService.getAllRooms(id, pageNumber, pageSize);
            return ResponseEntity.ok(new ApiResponse("00", null, result));

    }

    // 4. Xem chi tiết phòng
//    @GetMapping("/{id}")
//    public ResponseEntity<RoomDTO> getRoomDetail(@PathVariable Long id) {
//        return ResponseEntity.ok(roomService.getRoomDetail(id));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok(new ApiResponse("00", null, "Xóa phòng thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("exception", e.getMessage(), null));
        }
    }
}