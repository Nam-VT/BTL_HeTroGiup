package it4341.HeTroGiup.service;

// 1. Import các Class trong dự án của bạn
import it4341.HeTroGiup.dto.AreaTypeDTO;
import it4341.HeTroGiup.entity.AreaType;
import it4341.HeTroGiup.repository.AreaTypeRepository;

// 2. Import Lombok
import lombok.RequiredArgsConstructor;

// 3. Import Spring Framework
import org.springframework.stereotype.Service;

// 4. Import Java Utilities
import java.util.List;
import java.util.stream.Collectors; // Dùng nếu Java < 16

@Service
@RequiredArgsConstructor // Tự động inject Repository
public class AreaTypeService {

    private final AreaTypeRepository areaTypeRepository;

    public List<AreaTypeDTO> getAllAreaTypes() {
        // Lấy danh sách Entity từ DB (chỉ lấy cái chưa xóa)
        List<AreaType> entities = areaTypeRepository.findAllByIsDeletedFalse();

        // Chuyển đổi từ Entity sang DTO dùng Stream API
        return entities.stream()
                .map(entity -> AreaTypeDTO.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .build())
                .toList();
    }
}