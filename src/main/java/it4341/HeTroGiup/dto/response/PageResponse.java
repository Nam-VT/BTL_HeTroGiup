package it4341.HeTroGiup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private List<T> data;
}