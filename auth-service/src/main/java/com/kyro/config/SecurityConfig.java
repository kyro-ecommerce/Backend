package com.kyro.config;

import com.kyro.auth.security.jwt.AuthTokenFilter;
import com.kyro.auth.security.jwt.JwtEntryPoint;
import com.kyro.auth.security.oauth2.OAuth2FailureHandler;
import com.kyro.auth.security.oauth2.OAuth2SuccessHandler;
import com.kyro.auth.security.userdetails.AppUserDetailsService;
import com.kyro.utils.ErrorResponseUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Value("${api.prefix}")
  private String API;

  private final AppUserDetailsService userDetailsService;
  private final JwtEntryPoint authEntryPoint;
  private final AuthTokenFilter authTokenFilter;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final OAuth2FailureHandler oAuth2FailureHandler;
  private final ErrorResponseUtils errorResponseUtils;
  private final PasswordEncoder passwordEncoder;

  public SecurityConfig(
      AppUserDetailsService userDetailsService,
      JwtEntryPoint authEntryPoint,
      AuthTokenFilter authTokenFilter,
      OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
      OAuth2SuccessHandler oAuth2SuccessHandler,
      OAuth2FailureHandler oAuth2FailureHandler,
      ErrorResponseUtils errorResponseUtils,
      PasswordEncoder passwordEncoder) {
    this.userDetailsService = userDetailsService;
    this.authEntryPoint = authEntryPoint;
    this.authTokenFilter = authTokenFilter;
    this.oAuth2UserService = oAuth2UserService;
    this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    this.oAuth2FailureHandler = oAuth2FailureHandler;
    this.errorResponseUtils = errorResponseUtils;
    this.passwordEncoder = passwordEncoder;
  }

  // CloudflareFilter and RateLimitFilter removed

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    var authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) ->
        errorResponseUtils.sendAccessDeniedError(
            response, "You do not have permission to access this resource.");
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    List<String> securedUrls = List.of(API + "/cart/**", API + "/cartItems/**", API + "/orders/**");

    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(API + "/auth/**")
                    .permitAll()
                    .requestMatchers(API + "/users/internal/**")
                    .permitAll()
                    .requestMatchers(API + "/categories/**")
                    .permitAll()
                    .requestMatchers(API + "/products/**")
                    .permitAll()
                    .requestMatchers(API + "/contact/info")
                    .permitAll()
                    .requestMatchers(API + "/chatbot/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers(API + "/payment/vnpay-callback")
                    .permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**", "/scalar/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers(API + "/admin/**")
                    .hasAuthority("ADMIN")
                    .requestMatchers(API + "/customer/**")
                    .hasAnyAuthority("CUSTOMER")
                    .requestMatchers(securedUrls.toArray(String[]::new))
                    .authenticated()
                    .requestMatchers("/oauth2/**", "/login/oauth2/code/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauth2 ->
                oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler))
        .authenticationProvider(authenticationProvider())
        // Removed filter mappings
        .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
