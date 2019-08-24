package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource


@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class SecurityConfiguration(private val userDetailsService: CustomUserDetailsService) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()
                .authorizeExchange().pathMatchers("/auth", "/login").permitAll()
                .and()
                .authorizeExchange().pathMatchers("/polls/**", "/votes/**").authenticated()
                .and()
                .formLogin()
                .authenticationSuccessHandler(RedirectServerAuthenticationSuccessHandler("/auth/get"))
                //.authenticationManager(authenticationManager)
                //.loginPage("/login")
                //.authenticationFailureHandler { exchange, exception -> Mono.error(exception) }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun reactiveAuthenticationManager(): UserDetailsRepositoryReactiveAuthenticationManager {
        val authManager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
        authManager.setPasswordEncoder(passwordEncoder())
        return authManager
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*") //""http://localhost:3000")
        configuration.allowedMethods = listOf("GET", "POST", "DELETE", "PUT")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

/*
@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class SecurityConfiguration {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()
                .authorizeExchange().pathMatchers("/auth", "/login").permitAll()
                .and()
                .authorizeExchange().pathMatchers("/polls", "/votes").authenticated()
                .and()
                .formLogin()
                .loginPage("/login")

        return http.build()
    }


    @Bean
    open fun passwordEncoderAndMatcher(): PasswordEncoder {
        return object : PasswordEncoder {
            override fun encode(rawPassword: CharSequence?): String {
                return BCryptPasswordEncoder().encode(rawPassword)
            }
            override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
                return BCryptPasswordEncoder().matches(rawPassword, encodedPassword)
            }
        }
    }

//    @Bean
//    fun authenticationManager(@Autowired userRepository: ReactiveUserDetailsService): ReactiveAuthenticationManager {
//        return UserDetailsRepositoryReactiveAuthenticationManager(userRepository)
//    }
}
*/