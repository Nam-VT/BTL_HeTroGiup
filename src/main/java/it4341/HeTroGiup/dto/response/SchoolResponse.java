package it4341.HeTroGiup.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchoolResponse {
    Long id;
    String name;
    String nameSchool;

}
