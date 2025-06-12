package com.orange.moviebackend.common.config.shiro;

// JwtToken 和 JwtUtil 需要确保存在且功能正确
import com.orange.moviebackend.common.config.shiro.JwtToken;
import com.orange.moviebackend.common.utils.JwtUtil;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtFilter extends BasicHttpAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    public static final String TOKEN_HEADER = "Token";
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:5173",
            "http://localhost:8080"
    );

    public static final String USER_INFO_ATTRIBUTE = "LOGIN_USER_INFO";
    private static final AntPathMatcher internalPathMatcher = new AntPathMatcher();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final List<String> ANONYMOUS_PATHS_INTERNAL_CHECK = Arrays.asList(
            "/sysUser/register",
            "/sysUser/login",
            "/images/**",
            "/tmdb/**",
            "/sysCinema",
            "/sysCinema/find/**",
            "/sysMovie/find/**",
            "/sysMovieCategory/find/**",
            "/sysSession",
            "/sysSession/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/viewing/internal/**",
            "/favicon.ico"
    );

    private boolean isPathConsideredAnonymousInternal(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        for (String pattern : ANONYMOUS_PATHS_INTERNAL_CHECK) {
            if (internalPathMatcher.match(pattern, requestURI)) {
                log.trace("isPathConsideredAnonymousInternal: URI [{}] matched internal anonymous pattern [{}]", requestURI, pattern);
                return true;
            }
        }
        return false;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(token)) {
            log.trace("createToken: Token found in header for URI: {}", httpRequest.getRequestURI());
            return new JwtToken(token);
        }
        log.trace("createToken: No token found in header for URI: {}", httpRequest.getRequestURI());
        return null;
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        String token = ((HttpServletRequest) request).getHeader(TOKEN_HEADER);
        boolean attempt = StringUtils.hasText(token);
        log.trace("isLoginAttempt for URI [{}]: {}", ((HttpServletRequest) request).getRequestURI(), attempt);
        return attempt;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        log.debug("executeLogin for URI: {}", httpRequest.getRequestURI());
        AuthenticationToken token = createToken(request, response);
        if (token == null) {
            log.warn("executeLogin: createToken returned null...");
            return false;
        }
        try {
            org.apache.shiro.subject.Subject subject = getSubject(request, response);
            subject.login(token);

            Object principal = subject.getPrincipal();
            request.setAttribute(USER_INFO_ATTRIBUTE, principal);

            log.info("executeLogin: JWT authentication successful for URI: {}. User principal set in request.", httpRequest.getRequestURI());
            return true;
        } catch (AuthenticationException e) {
            log.warn("executeLogin: JWT authentication failed for URI: {}. Reason: {}",
                    httpRequest.getRequestURI(), e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        log.debug("isAccessAllowed for URI: {} (mappedValue: {})", httpRequest.getRequestURI(), mappedValue);

        if (isPathConsideredAnonymousInternal(httpRequest)) {
            log.warn("isAccessAllowed: URI [{}] was EXPECTED to be anonymous by ShiroConfig but still reached JWT filter. Allowing access based on internal check (TEMPORARY).", httpRequest.getRequestURI());
            return true;
        }

        if (isLoginAttempt(request, response)) {
            try {
                return executeLogin(request, response);
            } catch (Exception e) {
                log.error("isAccessAllowed: Unexpected exception during executeLogin for URI [{}]: {}",
                        httpRequest.getRequestURI(), e.getMessage(), e);
                return false;
            }
        } else {
            log.warn("isAccessAllowed: Access DENIED (no token provided) for protected URI: {}", httpRequest.getRequestURI());
            return false;
        }
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        log.warn("onAccessDenied for URI: {}. Responding with 401.", httpRequest.getRequestURI());
        sendUnauthorizedResponse(response, "Unauthorized: Access Denied. A valid token is required or the provided token is invalid.");
        return false;
    }

    private void sendUnauthorizedResponse(ServletResponse response, String message) {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpResponse.setContentType("application/json;charset=UTF-8");
        try {
            String jsonError = String.format("{\"code\": %d, \"message\": \"%s\"}",
                    HttpStatus.UNAUTHORIZED.value(),
                    message);
            httpResponse.getWriter().write(jsonError);
        } catch (IOException e) {
            log.error("Failed to write 401 error response: {}", e.getMessage(), e);
        }
    }


    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        log.trace("JwtFilter preHandle for: {} {} from Origin: {}",
                httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getHeader("Origin"));

        String requestURI = httpRequest.getRequestURI();
        if (pathMatcher.match("/viewing/internal/**", requestURI)) {
            log.info("preHandle: Internal API call detected [{}], bypassing all JWT filter logic.", requestURI);
            return true;
        }

        String requestOrigin = httpRequest.getHeader("Origin");

        if (StringUtils.hasText(requestOrigin) && ALLOWED_ORIGINS.contains(requestOrigin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", requestOrigin);
        } else if (StringUtils.hasText(requestOrigin)) {
            log.warn("preHandle: Origin {} is not in the allowed list ({}).", requestOrigin, ALLOWED_ORIGINS);
        } else {
            log.trace("preHandle: No Origin header present in the request.");
        }

        httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
        String requestHeaders = httpRequest.getHeader("Access-Control-Request-Headers");
        if (StringUtils.hasText(requestHeaders)) {
            httpResponse.setHeader("Access-Control-Allow-Headers", requestHeaders);
        } else {
            httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, " + TOKEN_HEADER + ", Authorization, X-Requested-With");
        }
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(httpRequest.getMethod())) {
            log.info("preHandle: Handling OPTIONS pre-flight request for: {}", httpRequest.getRequestURI());
            if (StringUtils.hasText(requestOrigin) && ALLOWED_ORIGINS.contains(requestOrigin)) {
                httpResponse.setStatus(HttpStatus.OK.value());
            } else if (!StringUtils.hasText(requestOrigin)) {
                httpResponse.setStatus(HttpStatus.OK.value());
                log.warn("preHandle: OPTIONS request from non-allowed origin {} (or no origin). Responding OK but browser might block if Access-Control-Allow-Origin is not set correctly for it.", requestOrigin);
            } else {
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                log.warn("preHandle: OPTIONS request from non-allowed origin {}. Responding with status code that might lead to browser blocking.", requestOrigin);
            }
            return false;
        }

        log.trace("preHandle: Proceeding with super.preHandle for non-OPTIONS request: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
        return super.preHandle(request, response);
    }

    private String tokenAbbreviated(String token) {
        if (!StringUtils.hasText(token) || token.length() <= 10) {
            return token;
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}