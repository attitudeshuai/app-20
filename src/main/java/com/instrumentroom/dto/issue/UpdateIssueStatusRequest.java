package com.instrumentroom.dto.issue;

import com.instrumentroom.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 更新反馈状态请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIssueStatusRequest {

    @NotNull(message = "状态不能为空")
    private IssueStatus status;
}
