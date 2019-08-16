package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@EnableWebFluxSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
                .withUser("admin").password("password").roles("ADMIN")
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/")
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                .authorizeRequests().antMatchers("/public").permitAll()
                .and()
                .authorizeRequests().antMatchers("/authenticated", "/success").authenticated()
                .and()
                // Включает Form-based аутентификацию
                .formLogin()
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
}