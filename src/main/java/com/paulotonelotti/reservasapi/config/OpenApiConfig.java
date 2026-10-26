package com.paulotonelotti.reservasapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI reservasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reservas API")
                        .version("1.0.0")
                        .description("""
                                API para gerenciamento de reservas de espaços.

                                **Autenticação**
                                1. Cadastre-se em `POST /auth/registrar` e faça login em `POST /auth/login`.
                                2. Copie o `token` da resposta, clique em **Authorize** e cole o token.
                                3. As próximas requisições saem com o cabeçalho `Authorization: Bearer <token>`.

                                **Regras principais**
                                - Não é permitido reservar um espaço em horário que conflite com outra reserva \
                                ativa do mesmo espaço.
                                - Toda reserva nasce `PENDENTE`; o valor total é calculado pelo servidor e \
                                congelado na reserva.
                                - Transições: `PENDENTE` → `CONFIRMADA` ou `CANCELADA`; `CONFIRMADA` → `CANCELADA`.

                                **Erros** seguem sempre o mesmo formato (`ErroResponse`). Erros de validação \
                                trazem também a lista de campos inválidos (`ErroValidacaoResponse`).
                                """)
                        .contact(new Contact().name("Paulo Tonelotti")))
                // Declara o esquema de autenticação: um token JWT no cabeçalho Authorization (Bearer)
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Aplica o esquema a todos os endpoints (o AuthController desliga isso para o login e o cadastro)
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
