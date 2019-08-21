package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.anyExchange
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
                .withUser("admin").password("password").roles("ADMIN")
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/")
    }

    public override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //.and()
                .authorizeRequests().antMatchers("/auth").permitAll()
                .and()
                .authorizeRequests().antMatchers("/polls", "/votes").authenticated()
                .and()
                .authorizeRequests().antMatchers("/user").hasRole("USER")
                .and()
                .formLogin()
                .and()
                .logout().logoutUrl("/logout")
    }

    /*@Bean
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
    }*/

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        //return NoOpPasswordEncoder.getInstance()
        return object : PasswordEncoder {
            override fun encode(charSequence: CharSequence): String {
                return charSequence.toString()
            }

            override fun matches(charSequence: CharSequence, s: String): Boolean {
                return charSequence.toString() == s
            }
        }
    }

    /*@Bean
    fun userDetailsService(): ReactiveUserDetailsService {
        val user = User
                .withUsername("user")
                .password("password")
                .roles("USER")
                .build()
        return MapReactiveUserDetailsService(user)
    }*/
}