package com.quinzex.filter;

import com.quinzex.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Component
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtService  jwtService;

    public JwtVerificationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request , HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      if(SecurityContextHolder.getContext().getAuthentication() != null) {
          filterChain.doFilter(request,response);
          return;
      }
      String authorizationHeader = request.getHeader("Authorization");
      if (authorizationHeader == null  || ! authorizationHeader.startsWith("Bearer ")) {
          filterChain.doFilter(request,response);
          return;
      }
      String token = authorizationHeader.substring(7);
      try{
          Claims claims = jwtService.parseToken(token);
          String email = claims.getSubject();
          List<String> roles = claims.get("roles", List.class);
          List<String> permissions = claims.get("permissions", List.class);
          List<SimpleGrantedAuthority> authorities = new ArrayList<>();
          if (roles != null) {
              roles.forEach(role ->
                      authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
              );
          }
          if (permissions != null) {
              permissions.forEach(permission ->
                      authorities.add(new SimpleGrantedAuthority(permission))
              );
          }
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email,null, authorities);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
      }catch(Exception e){
          SecurityContextHolder.clearContext();
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
      }
      filterChain.doFilter(request,response);
    }
}
