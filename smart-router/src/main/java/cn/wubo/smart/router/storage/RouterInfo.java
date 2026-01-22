package cn.wubo.smart.router.storage;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RouterInfo {
    private String endpoint;
    private String method;
    @Builder.Default
    private LocalDateTime requestTime = LocalDateTime.now();
    @Builder.Default
    private Boolean isRateLimit = false;
    @Builder.Default
    private Boolean isConsum = true;
    @Builder.Default
    private Boolean isProxy = false;
    private String targetEndpoint;
    @Builder.Default
    private Boolean isMap = false;
    private String mapContent;
    @Builder.Default
    private Boolean isBody = false;
    private String bodyContent;
}
