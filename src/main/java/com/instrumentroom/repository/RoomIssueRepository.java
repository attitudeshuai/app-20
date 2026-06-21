package com.instrumentroom.repository;

import com.instrumentroom.entity.IssueStatus;
import com.instrumentroom.entity.RoomIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 房间反馈数据访问层
 */
@Repository
public interface RoomIssueRepository extends JpaRepository<RoomIssue, Long> {

    Page<RoomIssue> findByReporterId(Long reporterId, Pageable pageable);

    Page<RoomIssue> findByRoomId(Long roomId, Pageable pageable);

    @Query("SELECT r FROM RoomIssue r WHERE " +
           "(:roomId IS NULL OR r.room.id = :roomId) AND " +
           "(:reporterId IS NULL OR r.reporter.id = :reporterId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:issueType IS NULL OR r.issueType = :issueType)")
    Page<RoomIssue> searchIssues(
            @Param("roomId") Long roomId,
            @Param("reporterId") Long reporterId,
            @Param("status") IssueStatus status,
            @Param("issueType") String issueType,
            Pageable pageable);

    long countByStatus(IssueStatus status);
}
