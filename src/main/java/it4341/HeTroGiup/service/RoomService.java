package it4341.HeTroGiup.service;

import it4341.HeTroGiup.dto.request.RoomCreateRequest;
import it4341.HeTroGiup.dto.request.RoomSurveyAnswerRequest;
import it4341.HeTroGiup.dto.request.RoomUpdateRequest;
import it4341.HeTroGiup.dto.response.PageResponse;
import it4341.HeTroGiup.dto.response.RoomCreateResponse;
import it4341.HeTroGiup.dto.response.RoomListResponse;
import it4341.HeTroGiup.dto.response.RoomUpdateResponse;
import it4341.HeTroGiup.entity.*;
import it4341.HeTroGiup.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final LandlordRepository landlordRepository;
    private final AttachFileRepository attachFileRepository;
    private final RoomImageRepository roomImageRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final AreaTypeRepository areaTypeRepository;

    @Transactional
    public RoomCreateResponse createRoom(RoomCreateRequest req) {
        // 1. Tìm chủ trọ
        Landlord landlord = landlordRepository.findById(req.getLandlordUserId())
                .orElseThrow(() -> new RuntimeException("Chủ trọ không tồn tại"));

        if (req.getAreaTypeId() != null && !areaTypeRepository.existsById(req.getAreaTypeId())) {
            throw new RuntimeException("Khu vực (AreaType) không tồn tại");
        }

        // 2. Tạo đối tượng Room (Chưa có ảnh)
        Room room = new Room();
        room.setLandlord(landlord);
        room.setAreaTypeId(req.getAreaTypeId());
        room.setTitle(req.getTitle());
        room.setDescription(req.getDescription());
        room.setAddress(req.getAddress());
        room.setLatitude(req.getLatitude());
        room.setLongitude(req.getLongitude());
        room.setPriceVnd(req.getPriceVnd());
        room.setAreaSqm(req.getAreaSqm());
        room.setRoomType(req.getRoomType());
        room.setStatus(req.getStatus());
        room.setIsDeleted(false);
        room.setAvgAmenity(null);
        room.setAvgSecurity(null);

        Room savedRoom = roomRepository.save(room);

        // 3. Xử lý logic gán Ảnh (Theo tham số mới: Bìa riêng, Thường riêng)
        List<RoomImage> roomImages = new ArrayList<>();

        // 3.1. Xử lý Ảnh Bìa (roomCoverImageId)
        if (req.getRoomCoverImageId() != null) {
            AttachFile coverFile = attachFileRepository.findById(req.getRoomCoverImageId()).orElse(null);
            if (coverFile != null) {
                RoomImage roomImage = RoomImage.builder()
                        .room(savedRoom)
                        .attachFile(coverFile)
                        .isCover(true) // Set là bìa
                        .isDeleted(false)
                        .build();
                roomImages.add(roomImage);
            }
        }

        // 3.2. Xử lý Ảnh Thường (roomNotCoverImageIds)
        if (req.getRoomNotCoverImageIds() != null && !req.getRoomNotCoverImageIds().isEmpty()) {
            for (Long fileId : req.getRoomNotCoverImageIds()) {
                // Tránh thêm trùng ảnh bìa nếu lỡ client gửi trùng
                if (req.getRoomCoverImageId() != null && fileId.equals(req.getRoomCoverImageId())) continue;

                AttachFile attachFile = attachFileRepository.findById(fileId).orElse(null);
                if (attachFile != null) {
                    RoomImage roomImage = RoomImage.builder()
                            .room(savedRoom)
                            .attachFile(attachFile)
                            .isCover(false) // Set là ảnh thường
                            .isDeleted(false)
                            .build();
                    roomImages.add(roomImage);
                }
            }
        }

        // Lưu batch tất cả ảnh
        if (!roomImages.isEmpty()) {
            roomImageRepository.saveAll(roomImages);
        }

        // 4. Xử lý Khảo sát (Survey Answers)
        if (req.getSurveyAnswers() != null && !req.getSurveyAnswers().isEmpty()) {
            List<SurveyAnswer> answers = new ArrayList<>();
            for (RoomSurveyAnswerRequest ansDto : req.getSurveyAnswers()) {
                SurveyQuestion question = surveyQuestionRepository.findById(ansDto.getSurveyQuestionId())
                        .orElseThrow(() -> new RuntimeException("Câu hỏi ID " + ansDto.getSurveyQuestionId() + " không tồn tại"));

                SurveyAnswer answer = SurveyAnswer.builder()
                        .room(savedRoom)
                        .surveyQuestion(question)
                        .point(ansDto.getPoint())
                        .isDeleted(false)
                        .build();
                answers.add(answer);
            }
            surveyAnswerRepository.saveAll(answers);
        }

        // 5. Trả về ID của phòng mới
        return new RoomCreateResponse(savedRoom.getId());
    }

    @Transactional
    public RoomUpdateResponse updateRoom(Long roomId, RoomUpdateRequest req) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        if (req.getAreaTypeId() != null) {
            if (!areaTypeRepository.existsById(req.getAreaTypeId())) {
                throw new RuntimeException("Khu vực không tồn tại");
            }
            room.setAreaTypeId(req.getAreaTypeId());
        }

        room.setTitle(req.getTitle());
        room.setDescription(req.getDescription());
        room.setAddress(req.getAddress());
        room.setLatitude(req.getLatitude());
        room.setLongitude(req.getLongitude());
        room.setPriceVnd(req.getPriceVnd());
        room.setAreaSqm(req.getAreaSqm());
        room.setRoomType(req.getRoomType());
        if (req.getStatus() != null) room.setStatus(req.getStatus());

        Room savedRoom = roomRepository.save(room);

        if (req.getRoomCoverImageId() != null || (req.getRoomNotCoverImageIds() != null && !req.getRoomNotCoverImageIds().isEmpty())) {
            roomImageRepository.deleteByRoomId(roomId);

            List<RoomImage> imagesToSave = new ArrayList<>();

            if (req.getRoomCoverImageId() != null) {
                AttachFile coverFile = attachFileRepository.findById(req.getRoomCoverImageId()).orElse(null);
                if (coverFile != null) {
                    imagesToSave.add(RoomImage.builder()
                            .room(savedRoom)
                            .attachFile(coverFile)
                            .isCover(true)
                            .isDeleted(false)
                            .build());
                }
            }

            if (req.getRoomNotCoverImageIds() != null) {
                for (Long imgId : req.getRoomNotCoverImageIds()) {
                    if (req.getRoomCoverImageId() != null && imgId.equals(req.getRoomCoverImageId())) continue;
                    AttachFile file = attachFileRepository.findById(imgId).orElse(null);
                    if (file != null) {
                        imagesToSave.add(RoomImage.builder()
                                .room(savedRoom)
                                .attachFile(file)
                                .isCover(false)
                                .isDeleted(false)
                                .build());
                    }
                }
            }
            if (!imagesToSave.isEmpty()) {
                roomImageRepository.saveAll(imagesToSave);
            }
        }

        if (req.getSurveyAnswers() != null) {
            surveyAnswerRepository.deleteByRoomId(roomId);

            List<SurveyAnswer> answersToSave = new ArrayList<>();
            for (RoomSurveyAnswerRequest ansReq : req.getSurveyAnswers()) {
                SurveyQuestion question = surveyQuestionRepository.findById(ansReq.getSurveyQuestionId()).orElse(null);
                if (question != null) {
                    answersToSave.add(SurveyAnswer.builder()
                            .room(savedRoom)
                            .surveyQuestion(question)
                            .point(ansReq.getPoint())
                            .isDeleted(false)
                            .build());
                }
            }
            if (!answersToSave.isEmpty()) {
                surveyAnswerRepository.saveAll(answersToSave);
            }
        }

        return new RoomUpdateResponse(savedRoom.getId());
    }

    public PageResponse<RoomListResponse> getAllRooms(Long landLordId, int pageNumber, int pageSize) {

        Landlord landlord = landlordRepository.findById(landLordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin chủ trọ!"));

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Room> roomPage = roomRepository.findByLandlordIdAndIsDeletedFalse(landLordId, pageable);

        // Tối ưu Query: Lấy danh sách areaTypeId từ list phòng để query tên 1 lần
        List<Long> areaTypeIds = roomPage.getContent().stream()
                .map(Room::getAreaTypeId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Tạo Map: <ID, Name> để tra cứu nhanh
        Map<Long, String> areaTypeMap = new HashMap<>();
        if (!areaTypeIds.isEmpty()) {
            List<AreaType> areas = areaTypeRepository.findAllById(areaTypeIds);
            areaTypeMap = areas.stream()
                    .collect(Collectors.toMap(AreaType::getId, AreaType::getName));
        }

        Map<Long, String> finalAreaTypeMap = areaTypeMap;
        List<RoomListResponse> responseList = roomPage.getContent().stream().map(room -> {
            RoomListResponse dto = new RoomListResponse();
            dto.setId(room.getId());
            dto.setLandlordUserId(room.getLandlord().getId());

            Long aId = room.getAreaTypeId();
            dto.setAreaTypeId(aId);
            if (aId != null) {
                dto.setAreaTypeName(finalAreaTypeMap.get(aId));
            }

            dto.setTitle(room.getTitle());
            dto.setDescription(room.getDescription());
            dto.setAddress(room.getAddress());
            dto.setLatitude(room.getLatitude());
            dto.setLongitude(room.getLongitude());
            dto.setPriceVnd(room.getPriceVnd());
            dto.setAreaSqm(room.getAreaSqm());
            dto.setRoomType(room.getRoomType());
            dto.setStatus(room.getStatus());
            dto.setAvgAmenity(room.getAvgAmenity());
            dto.setAvgSecurity(room.getAvgSecurity());

            if (room.getImages() != null && !room.getImages().isEmpty()) {
                RoomImage coverImage = room.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsCover()))
                        .findFirst()
                        .orElse(room.getImages().get(0));

                dto.setRoomCoverImageId(coverImage.getAttachFile().getId());
                dto.setRoomCoverImageUrl(coverImage.getAttachFile().getUrl());
            }

            return dto;
        }).collect(Collectors.toList());

        return PageResponse.<RoomListResponse>builder()
                .pageNumber(roomPage.getNumber())
                .pageSize(roomPage.getSize())
                .totalElements(roomPage.getTotalElements())
                .totalPages(roomPage.getTotalPages())
                .data(responseList)
                .build();
    }

//    public RoomDTO getRoomDetail(Long roomId) {
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
//        return convertToDTO(room);
//    }


    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        roomImageRepository.deleteByRoomId(roomId);
        surveyAnswerRepository.deleteByRoomId(roomId);

        roomRepository.deleteById(roomId);
    }
}
