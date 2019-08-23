package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class SecurityConfiguration() {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()
                .authorizeExchange().pathMatchers("/auth", "/login").permitAll()
                .and()
                .authorizeExchange().pathMatchers("/polls/**", "/votes/**").authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
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
                //.authenticationFailureHandler { exchange, exception -> Mono.error(exception) }
                //.authenticationSuccessHandler(WebFilterChainServerAuthenticationSuccessHandler())
        return http.build()
    }

    @Bean
    fun userDetailsService(): ReactiveUserDetailsService {
//        val user = User
//                .withUsername("user")
//                .password("password")
//                .roles("USER")
//                .build()
//        return MapReactiveUserDetailsService(user)
        return userDetailsService
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