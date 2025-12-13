package it4341.HeTroGiup.repository;

import it4341.HeTroGiup.Enum.RoomType;
import it4341.HeTroGiup.entity.Room;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByIsDeletedFalse();

    @Modifying
    @Transactional
    @Query("UPDATE Room r SET r.isDeleted = true WHERE r.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Long> ids);

    String FILTER_QUERY = "SELECT r, rs.distance FROM Room r " +
            "LEFT JOIN RoomSchool rs ON (r.id = rs.roomId AND rs.schoolId = :schoolId) " +
            "WHERE r.isDeleted = false " +
            // Các filter cơ bản giữ nguyên
            "AND (:minPrice IS NULL OR r.priceVnd >= :minPrice) " +
            "AND (:maxPrice IS NULL OR r.priceVnd <= :maxPrice) " +
            "AND (:minArea IS NULL OR r.areaSqm >= :minArea) " +
            "AND (:maxArea IS NULL OR r.areaSqm <= :maxArea) " +
            "AND (:minSec IS NULL OR r.avgSecurity >= :minSec) " +
            "AND (:maxSec IS NULL OR r.avgSecurity <= :maxSec) " +
            "AND (:minAm IS NULL OR r.avgAmenity >= :minAm) " +
            "AND (:maxAm IS NULL OR r.avgAmenity <= :maxAm) " +
            "AND ((:areaTypeIds) IS NULL OR r.areaTypeId IN (:areaTypeIds)) " +
            "AND ((:roomTypes) IS NULL OR r.roomType IN (:roomTypes)) " +
            "AND (:schoolId IS NULL OR (rs.distance IS NOT NULL AND rs.distance >= :minDist AND rs.distance <= :maxDist))";

    // Hàm lấy phân trang (Trả về Page<Object[]>)
    @Query(FILTER_QUERY)
    Page<Object[]> findRoomsWithFilterPaged(
            @Param("schoolId") Long schoolId,
            @Param("minPrice") Long minPrice, @Param("maxPrice") Long maxPrice,
            @Param("minArea") Double minArea, @Param("maxArea") Double maxArea,
            @Param("minSec") Double minSec, @Param("maxSec") Double maxSec,
            @Param("minAm") Double minAm, @Param("maxAm") Double maxAm,
            @Param("minDist") Double minDist, @Param("maxDist") Double maxDist,
            @Param("areaTypeIds") List<Long> areaTypeIds,
            @Param("roomTypes") List<RoomType> roomTypes,
            Pageable pageable
    );

    // Hàm lấy TẤT CẢ để tạo Ma trận (Trả về List<Object[]>)
    @Query(FILTER_QUERY)
    List<Object[]> findAllRoomsWithFilter(
            @Param("schoolId") Long schoolId,
            @Param("minPrice") Long minPrice, @Param("maxPrice") Long maxPrice,
            @Param("minArea") Double minArea, @Param("maxArea") Double maxArea,
            @Param("minSec") Double minSec, @Param("maxSec") Double maxSec,
            @Param("minAm") Double minAm, @Param("maxAm") Double maxAm,
            @Param("minDist") Double minDist, @Param("maxDist") Double maxDist,
            @Param("areaTypeIds") List<Long> areaTypeIds,
            @Param("roomTypes") List<RoomType> roomTypes
    );
}