package it4341.HeTroGiup.controller;

import it4341.HeTroGiup.dto.RoomDTO;
import it4341.HeTroGiup.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 1. Thêm mới phòng
    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO));
    }

    // 2. Sửa thông tin phòng
    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    // 3. Xem danh sách phòng (Theo chủ trọ)
    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByLandlord(@PathVariable Long landlordId) {
        return ResponseEntity.ok(roomService.getRoomsByLandlord(landlordId));
    }

    // 4. Xem chi tiết phòng
    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomDetail(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomDetail(id));
    }
}