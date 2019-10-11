package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler
import reactor.core.publisher.Mono


@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class SecurityConfiguration(private val userDetailsService: CustomUserDetailsService) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()
                .authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll()
                .and()
                .authorizeExchange().pathMatchers("/browser/**", "/admin_users/**").hasAuthority(Roles.Admin.role)
                .and()
                .authorizeExchange().pathMatchers("/submit", "/login", "/users", "/actuator/**").permitAll()
                .and()
                .authorizeExchange().pathMatchers("/polls/**", "/votes/**").authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .authenticationSuccessHandler(WebFilterChainServerAuthenticationSuccessHandler())
                .authenticationFailureHandler { _, exception -> Mono.error(exception) }
                .and()
                .exceptionHandling()
                .authenticationEntryPoint { _, exception -> Mono.error(exception) }
                .accessDeniedHandler { _, exception -> Mono.error(exception) }
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

//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val configuration = CorsConfiguration()
//        configuration.allowedOrigins = listOf("http://localhost:3000")
//        configuration.allowedMethods = listOf("OPTIONS", "GET", "POST", "DELETE", "PUT")
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", configuration)
//        return source
//    }

//    @Bean
//    fun corsFilter(): FilterRegistrationBean {
//        val source = UrlBasedCorsConfigurationSource()
//        val config = CorsConfiguration()
//        config.allowCredentials = true
//        config.addAllowedOrigin("*") // @Value: http://localhost:8080
//        config.addAllowedHeader("*")
//        config.addAllowedMethod("*")
//        source.registerCorsConfiguration("/**", config)
//        val bean = FilterRegistrationBean(CorsWebFilter(source))
//        bean.setOrder(0)
//        return bean
//    }

//    @Bean
//    fun corsWebFilter(): CorsWebFilter {
//        val config = CorsConfiguration()
//        config.allowCredentials = true
//        config.addAllowedOrigin("http://localhost:3000")
//        config.addAllowedHeader("*")
//        config.addAllowedMethod("*")
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", config)
//        return CorsWebFilter(source)
//    }

//    @Bean
//    fun corsConfigurer(): WebFluxConfigurer {
//        return object : WebFluxConfigurerComposite() {
//
//            override fun addCorsMappings(registry: CorsRegistry) {
//                registry.addMapping("**").allowedOrigins("*")
//                        .allowedMethods("*")
//                        .allowCredentials(true)
//            }
//        }
//    }
}
