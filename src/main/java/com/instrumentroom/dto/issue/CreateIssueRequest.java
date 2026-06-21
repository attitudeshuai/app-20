package com.instrumentroom.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 创建反馈请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateIssueRequest {

    @NotNull(message = "练习室ID不能为空")
    private Long roomId;

    @NotBlank(message = "问题类型不能为空")
    @Size(max = 50, message = "问题类型不能超过50个字符")
    private String issueType;

    @NotBlank(message = "问题描述不能为空")
    private String description;
}
