package it4341.HeTroGiup.service;

import it4341.HeTroGiup.Enum.RoomType;
import it4341.HeTroGiup.dto.request.*;
import it4341.HeTroGiup.dto.response.*;
import it4341.HeTroGiup.entity.*;
import it4341.HeTroGiup.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final RoomSchoolRepository roomSchoolRepository;

    private final RoutingService routingService;

    @Transactional
    public RoomCreateResponse createRoom(RoomCreateRequest req) {
        // 1. Tìm chủ trọ
        Landlord landlord = landlordRepository.findById(req.getLandlordUserId())
                .orElseThrow(() -> new RuntimeException("Chủ trọ không tồn tại"));

        if (req.getAreaTypeId() != null && !areaTypeRepository.existsById(req.getAreaTypeId())) {
            throw new RuntimeException("Khu vực (AreaType) không tồn tại");
        }

        // Biến lưu điểm tb
        BigDecimal avgAmenity = null;
        BigDecimal avgSecurity = null;

        Map<Long, SurveyQuestion> questionMap = new HashMap<>();

        if (req.getSurveyAnswers() != null && !req.getSurveyAnswers().isEmpty()) {
            double sumAmenity = 0;
            int countAmenity = 0;
            double sumSecurity = 0;
            int countSecurity = 0;

            // Lấy danh sách ID câu hỏi từ request
            List<Long> questionIds = req.getSurveyAnswers().stream()
                    .map(RoomSurveyAnswerRequest::getSurveyQuestionId)
                    .collect(Collectors.toList());

            // Fetch toàn bộ câu hỏi 1 lần từ DB
            List<SurveyQuestion> questions = surveyQuestionRepository.findAllById(questionIds);

            // Đưa vào Map để tra cứu nhanh
            questionMap = questions.stream()
                    .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

            // Duyệt qua từng câu trả lời trong request để tính toán
            for (RoomSurveyAnswerRequest ansDto : req.getSurveyAnswers()) {
                SurveyQuestion question = questionMap.get(ansDto.getSurveyQuestionId());

                if (question != null) {
                    Integer point = ansDto.getPoint();
                    if (point == null) point = 0;

                    // Kiểm tra loại khảo sát (AMENITY hay SECURITY)
                    // Lưu ý: Cần đảm bảo Entity SurveyType import đúng
                    if (question.getSurvey().getType() == it4341.HeTroGiup.Enum.SurveyType.AMENITY) {
                        sumAmenity += point;
                        countAmenity++;
                    } else if (question.getSurvey().getType() == it4341.HeTroGiup.Enum.SurveyType.SECURITY) {
                        sumSecurity += point;
                        countSecurity++;
                    }
                }
            }

            // Tính trung bình (Làm tròn 1 chữ số thập phân)
            if (countAmenity > 0) {
                avgAmenity = BigDecimal.valueOf(sumAmenity / countAmenity);
            }
            if (countSecurity > 0) {
                avgSecurity = BigDecimal.valueOf(sumSecurity / countSecurity);
            }
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

        room.setAvgAmenity(avgAmenity);
        room.setAvgSecurity(avgSecurity);

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

        // 4. Lưu chi tiết câu trả lời vào bảng survey_answer
        if (req.getSurveyAnswers() != null && !req.getSurveyAnswers().isEmpty()) {
            List<SurveyAnswer> answers = new ArrayList<>();
            for (RoomSurveyAnswerRequest ansDto : req.getSurveyAnswers()) {
                SurveyQuestion question = questionMap.get(ansDto.getSurveyQuestionId());

                if (question == null) {
                    question = surveyQuestionRepository.findById(ansDto.getSurveyQuestionId())
                            .orElseThrow(() -> new RuntimeException("Câu hỏi ID " + ansDto.getSurveyQuestionId() + " không tồn tại"));
                }

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
    public RoomUpdateResponse updateRoom(RoomUpdateRequest req) {
        Room room = roomRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // 2. Lưu lại tọa độ cũ để so sánh
        BigDecimal oldLat = room.getLatitude();
        BigDecimal oldLng = room.getLongitude();

        if (req.getAreaTypeId() != null) {
            room.setAreaTypeId(req.getAreaTypeId());
        }
        room.setTitle(req.getTitle());
        room.setDescription(req.getDescription());
        room.setAddress(req.getAddress());

        // Cập nhật tọa độ mới
        room.setLatitude(req.getLatitude());
        room.setLongitude(req.getLongitude());

        room.setPriceVnd(req.getPriceVnd());
        room.setAreaSqm(req.getAreaSqm());
        room.setRoomType(req.getRoomType());
        if (req.getStatus() != null) room.setStatus(req.getStatus());

        Room savedRoom = roomRepository.save(room);

        // CHECK THAY ĐỔI TỌA ĐỘ
        boolean isLatChanged = !Objects.equals(oldLat, req.getLatitude());
        boolean isLngChanged = !Objects.equals(oldLng, req.getLongitude());

        if (isLatChanged || isLngChanged) {
            try {
                routingService.updateAllDistancesForRoom(savedRoom);
            } catch (Exception e) {
                System.err.println("Warning: Không thể cập nhật lại khoảng cách. Error: " + e.getMessage());
            }
        }

        if (req.getRoomCoverImageId() != null || (req.getRoomNotCoverImageIds() != null && !req.getRoomNotCoverImageIds().isEmpty())) {
            roomImageRepository.deleteByRoomId(req.getId());

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
            surveyAnswerRepository.deleteByRoomId(req.getId());

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

    public RoomFilterResponse getAllRooms(RoomFilterRequest req) {

        List<Long> areaTypeIds = (req.getAreaTypeIds() != null && !req.getAreaTypeIds().isEmpty()) ? req.getAreaTypeIds() : null;
        List<RoomType> roomTypes = (req.getRoomTypes() != null && !req.getRoomTypes().isEmpty()) ? req.getRoomTypes() : null;

        List<Object[]> allResults = roomRepository.findAllRoomsWithFilter(
                req.getSchoolId(),
                req.getFromPrice(), req.getToPrice(),
                req.getFromArea(), req.getToArea(),
                req.getFromSecurityPoints(), req.getToSecurityPoints(),
                req.getFromAmenityPoints(), req.getToAmenityPoints(),
                req.getFromDistance(), req.getToDistance(),
                areaTypeIds, roomTypes
        );

        List<Long> rowIdsInMatrix = new ArrayList<>();
        List<List<Double>> initMatrix = new ArrayList<>();

        for (Object[] row : allResults) {
            Room room = (Room) row[0];
            Double dist = (Double) row[1];

            rowIdsInMatrix.add(room.getId());

            double rawPrice    = room.getPriceVnd() != null ? room.getPriceVnd().doubleValue() : 0.0;
            double rawDistance = dist != null ? dist : 0.0;
            double rawArea     = room.getAreaSqm() != null ? room.getAreaSqm().doubleValue() : 0.0;
            double rawAmenity  = room.getAvgAmenity() != null ? room.getAvgAmenity().doubleValue() : 0.0;
            double rawSecurity = room.getAvgSecurity() != null ? room.getAvgSecurity().doubleValue() : 0.0;

            List<Double> matrixRow = Arrays.asList(
                    rawPrice,     // Cột 1: Giá
                    rawDistance,  // Cột 2: Khoảng cách
                    rawArea,      // Cột 3: Diện tích
                    rawAmenity,   // Cột 4: Tiện ích
                    rawSecurity   // Cột 5: An ninh
            );

            initMatrix.add(matrixRow);
        }

        Pageable pageable = PageRequest.of(req.getPageNumber(), req.getPageSize());
        Page<Object[]> pagedResults = roomRepository.findRoomsWithFilterPaged(
                req.getSchoolId(), req.getFromPrice(), req.getToPrice(), req.getFromArea(), req.getToArea(),
                req.getFromSecurityPoints(), req.getToSecurityPoints(), req.getFromAmenityPoints(), req.getToAmenityPoints(),
                req.getFromDistance(), req.getToDistance(), areaTypeIds, roomTypes, pageable
        );

        List<Long> aIds = pagedResults.getContent().stream()
                .map(obj -> ((Room) obj[0]).getAreaTypeId())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> areaMap = new HashMap<>();
        if (!aIds.isEmpty()) {
            areaMap = areaTypeRepository.findAllById(aIds).stream()
                    .collect(Collectors.toMap(AreaType::getId, AreaType::getName));
        }
        final Map<Long, String> finalAreaMap = areaMap;

        List<RoomListResponse> responseData = pagedResults.getContent().stream().map(obj -> {
            Room room = (Room) obj[0];
            Double dist = (Double) obj[1];

            RoomListResponse dto = new RoomListResponse();

            dto.setId(room.getId());
            dto.setLandlordUserId(room.getLandlord().getId());
            dto.setLandlordPhone(room.getLandlord().getPhoneNumber());

            dto.setAreaTypeId(room.getAreaTypeId());
            if (room.getAreaTypeId() != null) {
                dto.setAreaTypeName(finalAreaMap.get(room.getAreaTypeId()));
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

            dto.setDistance(dist);

            if (room.getImages() != null && !room.getImages().isEmpty()) {
                RoomImage cover = room.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsCover()))
                        .findFirst()
                        .orElse(room.getImages().get(0));

                dto.setRoomCoverImageId(cover.getAttachFile().getId());
                dto.setRoomCoverImageUrl(cover.getAttachFile().getUrl());

                List<RoomNotCoverImageResponse> notCoverImages = room.getImages().stream()
                        .filter(img -> !img.getId().equals(cover.getId()))
                        .map(img -> new RoomNotCoverImageResponse(
                                img.getAttachFile().getId(),
                                img.getAttachFile().getUrl()
                        ))
                        .collect(Collectors.toList());
                dto.setRoomNotCoverImages(notCoverImages);
            } else {
                dto.setRoomNotCoverImages(Collections.emptyList());
            }

            return dto;
        }).collect(Collectors.toList());

        return RoomFilterResponse.builder()
                .pageNumber(pagedResults.getNumber())
                .pageSize(pagedResults.getSize())
                .totalElements(pagedResults.getTotalElements())
                .totalPages(pagedResults.getTotalPages())
                .rowIdsInMatrix(rowIdsInMatrix)
                .initMatrix(initMatrix)
                .data(responseData)
                .build();
    }

//    public RoomDTO getRoomDetail(Long roomId) {
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
//        return convertToDTO(room);
//    }


    @Transactional
    public void deleteRooms(RoomDeleteRequest request) {
        List<Long> ids = request.getIds();

        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Danh sách ID không được để trống");
        }
        roomRepository.softDeleteByIds(ids);

        roomImageRepository.softDeleteByRoomId(ids);
        surveyAnswerRepository.softDeleteByRoomId(ids);
        roomSchoolRepository.softDeleteByRoomId(ids);
    }


}
