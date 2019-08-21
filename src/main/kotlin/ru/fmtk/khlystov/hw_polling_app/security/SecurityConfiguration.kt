package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain


@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration { //}: WebSecurityConfigurerAdapter() {
//    override fun configure(auth: AuthenticationManagerBuilder) {
//        auth.inMemoryAuthentication()
//                .withUser("admin").password("password").roles("ADMIN")
//    }

//    override fun configure(web: WebSecurity) {
//        web.ignoring().antMatchers("/")
//    }

    //public override fun configure(http: HttpSecurity) {
//        http.csrf().disable()
//                //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                //.and()
//                .authorizeRequests().antMatchers("/auth/**", "/login/**").permitAll()
//                .and()
//                .authorizeRequests().antMatchers("/polls/**", "/votes/**").authenticated()
//                .and()
//                .authorizeRequests().antMatchers("/user/**").hasRole("USER")
//                .and()
//                .formLogin()
//    }

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
    fun passwordEncoder(): PasswordEncoder {
        //return NoOpPasswordEncoder.getInstance()
        return BCryptPasswordEncoder()
    }

    @Autowired
    lateinit var userRepository: ReactiveUserDetailsService

    @Bean
    fun userDetailsService(): ReactiveUserDetailsService {
//        val user = User
//                .withUsername("user")
//                .password("password")
//                .roles("USER")
//                .build()
//        return MapReactiveUserDetailsService(user)
        return userRepository
    }

    /*@Bean
    fun userDetailsService(): ReactiveUserDetailsService {

    }*/

//    @Bean
//    fun authenticationManager(@Autowired userRepository: ReactiveUserDetailsService): ReactiveAuthenticationManager {
//        return UserDetailsRepositoryReactiveAuthenticationManager(userRepository)
//    }
}