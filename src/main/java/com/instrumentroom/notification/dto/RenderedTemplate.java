package com.instrumentroom.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RenderedTemplate {
    private String subject;
    private String content;
}
