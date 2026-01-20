package org.hanseiro.server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    // JWT 인증 설정 (헤더에 토큰 입력 가능하게 함)
    String jwtSchemeName = "jwtAuth";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
    Components components = new Components()
        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
            .name(jwtSchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT"));

    return new OpenAPI()
        .components(components)
        .addSecurityItem(securityRequirement)
        .info(new Info()
            .title("한세로(Hanseiro) API")
            .description("한세로 택시 매칭 서비스 API 명세서")
            .version("v0.0.1"));
  }
}