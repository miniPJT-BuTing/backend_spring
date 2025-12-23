package com.mini.buting.api.health;

import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RequestMapping("/v1/health")
@RestController
public class HealthCheckController {

    @GetMapping()
    public BaseResponse<Void> checkHealthStatus() {
        return BaseResponse.onSuccess();
    }
}
