package cn.wubo.smart.router.storage;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RouterInfo {
    private String endpoint;
    @Builder.Default
    private LocalDateTime requestTime = LocalDateTime.now();
    @Builder.Default
    private Boolean isRateLimit = false;
    @Builder.Default
    private Boolean isConsum = true;
    @Builder.Default
    private Boolean isProxy = false;
    private String targetEndpoint;
}
