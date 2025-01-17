package org.gdgoc.server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(servers = {@Server(url = "/", description = "default generated url")})
@Configuration
public class SpringDocOpenApiConfig {

    private Info info() {
        return new Info()
                .title("GDG 해커톤 백야")
                .version("v0.1")
                .description("GDG 해커톤 백야 19팀 API 명세서입니다.");
    }

}