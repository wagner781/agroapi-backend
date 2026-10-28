package pe.upeu.agrosierra.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.*
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.security.web.SecurityFilterChain
import pe.upeu.agrosierra.web.dto.ErrorRespuesta

@Configuration @EnableMethodSecurity
class SecurityConfig(private val mapper: ObjectMapper) {
    @Bean fun passwordEncoder() = BCryptPasswordEncoder()
    @Bean fun filtro(http: HttpSecurity): SecurityFilterChain {
        val authorities = JwtGrantedAuthoritiesConverter().apply { setAuthoritiesClaimName("privilegios"); setAuthorityPrefix("") }
        val converter = JwtAuthenticationConverter().apply { setJwtGrantedAuthoritiesConverter(authorities) }
        val entry = org.springframework.security.web.AuthenticationEntryPoint { r, s, _ ->
            s.status=401; s.contentType="application/json"; mapper.writeValue(s.outputStream,
                ErrorRespuesta(status=401,error="Unauthorized",message="Sesion ausente, invalida o vencida",path=r.requestURI)) }
        val denied = org.springframework.security.web.access.AccessDeniedHandler { r, s, _ ->
            s.status=403; s.contentType="application/json"; mapper.writeValue(s.outputStream,
                ErrorRespuesta(status=403,error="Forbidden",message="No tiene el privilegio necesario",path=r.requestURI)) }
        http.csrf { it.disable() }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.requestMatchers(HttpMethod.POST,"/api/v1/auth/login").permitAll().anyRequest().authenticated() }
            .exceptionHandling { it.authenticationEntryPoint(entry).accessDeniedHandler(denied) }
            .oauth2ResourceServer { it.jwt { j -> j.jwtAuthenticationConverter(converter) }.authenticationEntryPoint(entry).accessDeniedHandler(denied) }
        // BearerTokenAuthenticationFilter de Spring verifica firma/exp/issuer antes de llegar a los servicios.
        return http.build()
    }
}
