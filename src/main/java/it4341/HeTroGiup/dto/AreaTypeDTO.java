package it4341.HeTroGiup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaTypeDTO {
    private Long id;
    private String name;
    // Không bao gồm isDeleted ở đây
}