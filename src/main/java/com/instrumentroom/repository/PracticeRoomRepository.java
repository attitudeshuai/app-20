package com.instrumentroom.repository;

import com.instrumentroom.entity.PracticeRoom;
import com.instrumentroom.entity.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 练习室数据访问层
 */
@Repository
public interface PracticeRoomRepository extends JpaRepository<PracticeRoom, Long> {

    Page<PracticeRoom> findByStatus(RoomStatus status, Pageable pageable);

    @Query("SELECT r FROM PracticeRoom r WHERE " +
           "(:name IS NULL OR r.name LIKE %:name%) AND " +
           "(:location IS NULL OR r.location LIKE %:location%) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<PracticeRoom> searchRooms(
            @Param("name") String name,
            @Param("location") String location,
            @Param("status") RoomStatus status,
            Pageable pageable);

    List<PracticeRoom> findByStatusOrderByCreatedAtDesc(RoomStatus status);
}
