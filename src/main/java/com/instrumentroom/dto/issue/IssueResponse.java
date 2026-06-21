package com.instrumentroom.dto.issue;

import com.instrumentroom.dto.auth.UserResponse;
import com.instrumentroom.dto.room.RoomResponse;
import com.instrumentroom.entity.IssueStatus;
import com.instrumentroom.entity.RoomIssue;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 反馈响应DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueResponse {

    private Long id;
    private Long roomId;
    private RoomResponse room;
    private Long reporterId;
    private UserResponse reporter;
    private String issueType;
    private String description;
    private IssueStatus status;
    private LocalDateTime createdAt;

    public static IssueResponse fromEntity(RoomIssue issue, boolean includeDetails) {
        IssueResponseBuilder builder = IssueResponse.builder()
                .id(issue.getId())
                .roomId(issue.getRoom().getId())
                .reporterId(issue.getReporter().getId())
                .issueType(issue.getIssueType())
                .description(issue.getDescription())
                .status(issue.getStatus())
                .createdAt(issue.getCreatedAt());

        if (includeDetails) {
            builder.room(RoomResponse.fromEntity(issue.getRoom()));
            builder.reporter(UserResponse.fromEntity(issue.getReporter()));
        }

        return builder.build();
    }
}
