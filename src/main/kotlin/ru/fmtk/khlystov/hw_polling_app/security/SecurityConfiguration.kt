package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.anyExchange
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler
import reactor.core.publisher.Mono


@EnableWebFluxSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
                .withUser("admin").password("password").roles("ADMIN")
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()
                .authorizeExchange().pathMatchers("/auth").permitAll()
                .and()
                .authorizeExchange().pathMatchers("/polls", "/votes").authenticated()
                .and()
                .formLogin()
                .loginPage("/auth")
                .authenticationFailureHandler { exchange, exception -> Mono.error(exception) }
                .authenticationSuccessHandler(WebFilterChainServerAuthenticationSuccessHandler())
        return http.build()
    }
}