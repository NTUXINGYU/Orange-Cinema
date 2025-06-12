package com.orange.moviebackend.common.config.shiro.realms;

import com.orange.moviebackend.common.config.shiro.JwtToken;
import com.orange.moviebackend.common.utils.JwtUtil;
import com.orange.moviebackend.domain.LoginUser;
import com.orange.moviebackend.domain.SysUser;
import com.orange.moviebackend.service.SysUserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class CustomerRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(CustomerRealm.class);

    private SysUserService sysUserService;

    public void setSysUserService(SysUserService sysUserService) {
        if (sysUserService == null) {
            log.error("SysUserService in CustomerRealm is not injected! Authentication will fail.");
        }
        this.sysUserService = sysUserService;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.debug("doGetAuthorizationInfo called for principals: {}", principals);

        LoginUser loginUser = (LoginUser) principals.getPrimaryPrincipal();

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        if (loginUser != null && loginUser.getSysUser() != null && loginUser.getSysUser().getSysRole() != null) {
            simpleAuthorizationInfo.addRole(loginUser.getSysUser().getSysRole().getRoleName());
        }

        return simpleAuthorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {

        log.debug("doGetAuthenticationInfo called with token: {}", authenticationToken);

        if (this.sysUserService == null) {
            log.error("SysUserService is not available in CustomerRealm. Cannot perform authentication.");
            throw new AuthenticationException("Realm a_inner configuration error: SysUserService not set.");
        }

        String jwtString = (String) authenticationToken.getCredentials();

        if (!StringUtils.hasText(jwtString)) {
            log.warn("doGetAuthenticationInfo: JWT string is empty or null.");
            throw new AuthenticationException("Token cannot be empty.");
        }
        log.debug("doGetAuthenticationInfo: Processing JWT: {}", jwtString.length() > 10 ? jwtString.substring(0, 10) + "..." : jwtString);

        String username = null;
        try {
            username = JwtUtil.getUsername(jwtString);
        } catch (Exception e) {
            log.warn("doGetAuthenticationInfo: Failed to get username from JWT. JWT: {}, Error: {}", jwtString, e.getMessage());
            throw new AuthenticationException("Invalid token: could not parse username.", e);
        }

        if (!StringUtils.hasText(username)) {
            log.warn("doGetAuthenticationInfo: Username extracted from JWT is empty or null.");
            throw new AuthenticationException("Invalid token: username is missing.");
        }
        log.debug("doGetAuthenticationInfo: Username from JWT: {}", username);

        SysUser user = sysUserService.findUserByName(username);
        if (user == null) {
            log.warn("doGetAuthenticationInfo: User not found in database for username: {}", username);
            throw new UnknownAccountException("User '" + username + "' does not exist.");
        }
        log.debug("doGetAuthenticationInfo: User found in DB: {}", user.getUserName());

        try {
            if (!JwtUtil.verify(jwtString, username, user.getPassword())) {
                log.warn("doGetAuthenticationInfo: JWT verification failed for user: {}", username);
                throw new IncorrectCredentialsException("Token verification failed (e.g., signature mismatch or invalid).");
            }
        } catch (Exception e) {
            log.warn("doGetAuthenticationInfo: Exception during JWT verification for user: {}. Error: {}", username, e.getMessage());
            if (e instanceof ExpiredCredentialsException) {
                throw e;
            }
            throw new AuthenticationException("Token verification process failed.", e);
        }

        log.info("doGetAuthenticationInfo: Authentication successful for user: {}", username);

        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);

        return new SimpleAuthenticationInfo(loginUser, jwtString, getName());
    }
}