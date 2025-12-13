package it4341.HeTroGiup.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomDeleteRequest {
    private List<Long> ids;
}
