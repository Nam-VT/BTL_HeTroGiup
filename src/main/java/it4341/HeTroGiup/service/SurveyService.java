package it4341.HeTroGiup.service;

import it4341.HeTroGiup.dto.SurveyDTO;
import it4341.HeTroGiup.entity.Survey;
import it4341.HeTroGiup.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public List<SurveyDTO> getAllSurveys() {
        List<Survey> list = surveyRepository.findAllByIsDeletedFalse();

        if (list.isEmpty()) {
            throw new RuntimeException("Không có bài khảo sát nào trong hệ thống.");
        }

        return list.stream().map(e -> SurveyDTO.builder()
                .id(e.getId())
                .type(e.getType())
                .title(e.getTitle())
                .description(e.getDescription())
                .build()
        ).toList();
    }
}