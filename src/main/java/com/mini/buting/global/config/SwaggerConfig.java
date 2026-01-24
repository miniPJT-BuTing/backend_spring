package com.mini.buting.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.mini.buting.global.security.constant.SecurityConstants.Token.GRANT_TYPE;

@Configuration
public class SwaggerConfig {
    @Value("${swagger.uri}")
    private String uri;

    @Bean
    OpenAPI openAPI() {
        String securityJwtName = "JWT";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityJwtName);

        Components components = new Components().addSecuritySchemes(securityJwtName,
                new SecurityScheme()
                        .name(securityJwtName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(GRANT_TYPE.trim())
                        .bearerFormat(securityJwtName)
                        .description("헤더에 'Authorization: %s{ACCESS_TOKEN}' 형식으로 토큰을 입력해주세요.".formatted(GRANT_TYPE)));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .addServersItem(new Server().url(uri))
                .info(info());
    }

    private Info info() {
        return new Info()
                .title("Buting Spring API Docs")
                .description("부울경 과팅을 위한 모바일 앱 서비스 '부팅' API 명세서")
                .version("1.0.0");
    }

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1-api")
                .pathsToMatch("/v1/**") // 이 경로에 해당하는 API 문서화
                .build();
    }
}
