package com.paulotonelotti.reservasapi.security;

import com.paulotonelotti.reservasapi.dto.response.LoginResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiracao-minutos}")
    private Long expiracaoMinutos;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponseDTO gerarToken(UsuarioAutenticado usuarioAutenticado) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plusSeconds(expiracaoMinutos);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("reservasapi")
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuarioAutenticado.getUsuario().getId().toString())
                .claim("roles", List.of(usuarioAutenticado.getUsuario().getPapel().name()));

        if (usuarioAutenticado.getUsuario().getCliente() != null) {
            claims.claim("clienteId", usuarioAutenticado.getUsuario().getCliente().getId().toString());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(cabecalho, claims.build()))
                .getTokenValue();

        return new LoginResponseDTO(token, "Bearer", expiraEm);
    }
}
