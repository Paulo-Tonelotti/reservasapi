package com.paulotonelotti.reservasapi.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.paulotonelotti.reservasapi.dto.response.LoginResponseDTO;
import com.paulotonelotti.reservasapi.entity.Usuario;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiracao-minutos}")
    private long expiracaoMinutos;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponseDTO gerarToken(UsuarioAutenticado usuarioAutenticado) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracaoMinutos, ChronoUnit.MINUTES);

        Usuario usuario = usuarioAutenticado.getUsuario(); // pega o usuario do autenticado

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("reservasapi")
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getId().toString())
                .claim("roles", List.of(usuario.getPapel().name()));

        if (usuario.getCliente() != null) {
            claims.claim("clienteId", usuario.getCliente().getId().toString());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(cabecalho, claims.build()))
                .getTokenValue();

        return new LoginResponseDTO(token, "Bearer", expiraEm);
    }
}
