package com.instrumentroom.dto.issue;

import com.instrumentroom.entity.IssueStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 更新反馈请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIssueRequest {

    @Size(max = 50, message = "问题类型不能超过50个字符")
    private String issueType;

    private String description;

    private IssueStatus status;
}
