package org.gdgoc.server.common;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GdgApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder
    public GdgApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}


