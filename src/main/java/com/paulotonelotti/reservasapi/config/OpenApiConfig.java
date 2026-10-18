package com.paulotonelotti.reservasapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reservasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reservas API")
                        .version("1.0.0")
                        .description("""
                                API para gerenciamento de reservas de espaços.

                                **Regras principais**
                                - Não é permitido reservar um espaço em horário que conflite com outra reserva \
                                ativa do mesmo espaço.
                                - Toda reserva nasce `PENDENTE`; o valor total é calculado pelo servidor e \
                                congelado na reserva.
                                - Transições: `PENDENTE` → `CONFIRMADA` ou `CANCELADA`; `CONFIRMADA` → `CANCELADA`.

                                **Erros** seguem sempre o mesmo formato (`ErroResponse`). Erros de validação \
                                trazem também a lista de campos inválidos (`ErroValidacaoResponse`).
                                """)
                        .contact(new Contact().name("Paulo Tonelotti")));
    }
}
