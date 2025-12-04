package it4341.HeTroGiup.service;

import it4341.HeTroGiup.dto.RoomDTO;
import it4341.HeTroGiup.entity.*;
import it4341.HeTroGiup.Enum.RoomStatus;
import it4341.HeTroGiup.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final LandlordRepository landlordRepository;
    private final AttachFileRepository attachFileRepository;
    private final RoomImageRepository roomImageRepository;

    @Transactional
    public RoomDTO createRoom(RoomDTO dto) {
        Landlord landlord = landlordRepository.findById(dto.getLandlordId())
                .orElseThrow(() -> new RuntimeException("Chủ trọ không tồn tại"));

        Room room = new Room();
        room.setLandlord(landlord);
        room.setTitle(dto.getTitle());
        room.setDescription(dto.getDescription());
        room.setAddress(dto.getAddress());
        room.setLatitude(dto.getLatitude());
        room.setLongitude(dto.getLongitude());
        room.setPriceVnd(dto.getPriceVnd());
        room.setAreaSqm(dto.getAreaSqm());
        room.setRoomType(dto.getRoomType());
        room.setStatus(RoomStatus.PENDING);
        room.setIsDeleted(false);

        Room savedRoom = roomRepository.save(room);

        // Xử lý lưu ảnh (Tạo các bản ghi trong bảng RoomImage)
        if (dto.getImageIds() != null && !dto.getImageIds().isEmpty()) {
            saveRoomImages(savedRoom, dto.getImageIds());
        }

        return convertToDTO(savedRoom);
    }

    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomDTO dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        room.setTitle(dto.getTitle());
        room.setDescription(dto.getDescription());
        room.setAddress(dto.getAddress());
        room.setPriceVnd(dto.getPriceVnd());
        room.setAreaSqm(dto.getAreaSqm());
        room.setRoomType(dto.getRoomType());
        if(dto.getStatus() != null) room.setStatus(dto.getStatus());
        room.setLatitude(dto.getLatitude());
        room.setLongitude(dto.getLongitude());

        Room updatedRoom = roomRepository.save(room);

        if (dto.getImageIds() != null) {
            roomImageRepository.deleteByRoomId(roomId);
            saveRoomImages(updatedRoom, dto.getImageIds());
        }

        return convertToDTO(updatedRoom);
    }

    // --- 3. XEM DANH SÁCH (Theo chủ nhà) ---
    public List<RoomDTO> getRoomsByLandlord(Long landlordId) {
        List<Room> rooms = roomRepository.findByLandlordIdAndIsDeletedFalseOrderByIdDesc(landlordId);
        return rooms.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // --- 4. XEM CHI TIẾT ---
    public RoomDTO getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        return convertToDTO(room);
    }

    // Hàm chuyển Entity -> DTO
    private RoomDTO convertToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setLandlordId(room.getLandlord().getId());
        dto.setTitle(room.getTitle());
        dto.setDescription(room.getDescription());
        dto.setAddress(room.getAddress());
        dto.setPriceVnd(room.getPriceVnd());
        dto.setAreaSqm(room.getAreaSqm());
        dto.setRoomType(room.getRoomType());
        dto.setStatus(room.getStatus());
        dto.setLatitude(room.getLatitude());
        dto.setLongitude(room.getLongitude());
        dto.setAvgAmenity(room.getAvgAmenity());
        dto.setAvgScurity(room.getAvgScurity());

        if (room.getImages() != null && !room.getImages().isEmpty()) {
            List<String> urls = room.getImages().stream()
                    .map(roomImage -> roomImage.getAttachFile().getUrl())
                    .collect(Collectors.toList());
            dto.setImageUrls(urls);
        } else {
            dto.setImageUrls(Collections.emptyList());
        }

        return dto;
    }

    private void saveRoomImages(Room room, List<Long> fileIds) {
        List<RoomImage> imageList = new ArrayList<>();

        for (Long fileId : fileIds) {
            AttachFile file = attachFileRepository.findById(fileId).orElse(null);
            if (file != null) {
                RoomImage roomImage = RoomImage.builder()
                        .room(room)          // Link với Room
                        .attachFile(file)    // Link với File
                        .isCover(false)      // Mặc định false
                        .isDeleted(false)
                        .build();
                imageList.add(roomImage);
            }
        }
        if (!imageList.isEmpty()) {
            roomImageRepository.saveAll(imageList);
        }
    }
}