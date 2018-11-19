package com.isanuric.bar.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isanuric.bar.ldap.User;
import com.isanuric.bar.service.JwtService;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * Project: bar
 * Created by ehsan.salmani@ic-consult.de on 11/11/2018.
 */
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final static Logger logger = LoggerFactory.getLogger(CustomAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    private AuthenticationManager authenticationManager;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse httpServletResponse) throws AuthenticationException {

        logger.debug("start attemptAuthentication");

        try {
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            logger.debug("user: {}", user);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), new ArrayList<>())
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication auth) {

        logger.debug("start successfulAuthentication");
        response.addHeader(
                "Authorization",
                "Bearer " + jwtService.buildRequestToken(SecurityContextHolder.getContext().getAuthentication().getName()));
    }
}