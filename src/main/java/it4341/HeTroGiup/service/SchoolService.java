package it4341.HeTroGiup.service;

import it4341.HeTroGiup.dto.request.SchoolRequest;
import it4341.HeTroGiup.dto.response.SchoolResponse;
import it4341.HeTroGiup.entity.School;
import it4341.HeTroGiup.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public List<SchoolResponse> getAllSchool(SchoolRequest schoolRequest) {
        String nameSchool = schoolRequest.getName();
        List<School> schools;

        schools = schoolRepository.findByNameLike(nameSchool.trim());

        return schools.stream().map(school -> {
            return SchoolResponse.builder()
                    .id(school.getId())
                    .name(school.getSchoolName())
                    .nameSchool(school.getNameSearch())
                    .build();
        }).collect(Collectors.toList());
    }
}
