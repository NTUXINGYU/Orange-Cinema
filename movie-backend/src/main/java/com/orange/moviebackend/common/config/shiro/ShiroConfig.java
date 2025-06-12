package com.orange.moviebackend.common.config.shiro;

import com.orange.moviebackend.common.config.shiro.realms.CustomerRealm;
import com.orange.moviebackend.service.SysUserService;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.SecurityUtils;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public Realm realm(SysUserService sysUserService) {
        CustomerRealm realm = new CustomerRealm();
        realm.setSysUserService(sysUserService);
        return realm;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(Realm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);

        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            SecurityManager securityManager,
            JwtFilter jwtFilter) {

        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);

        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("jwt", jwtFilter);
        factoryBean.setFilters(filters);

        Map<String, String> filterChainMap = new LinkedHashMap<>();
        filterChainMap.put("/viewing/internal/**", "anon");
        filterChainMap.put("/sysUser/login", "anon");
        filterChainMap.put("/sysUser/register", "anon");
        filterChainMap.put("/images/**", "anon");
        filterChainMap.put("/tmdb/**", "anon");
        filterChainMap.put("/sysCinema", "anon");
        filterChainMap.put("/sysCinema/find/**", "anon");
        filterChainMap.put("/sysMovie/find/**", "anon");
        filterChainMap.put("/sysMovieCategory/find/**", "anon");
        filterChainMap.put("/sysSession", "anon");
        filterChainMap.put("/sysSession/**", "anon");
        filterChainMap.put("/swagger-ui/**", "anon");
        filterChainMap.put("/v3/api-docs/**", "anon");
        filterChainMap.put("/swagger-resources/**", "anon");
        filterChainMap.put("/webjars/**", "anon");
        filterChainMap.put("/favicon.ico", "anon");
        filterChainMap.put("/**", "jwt");


        factoryBean.setFilterChainDefinitionMap(filterChainMap);
        return factoryBean;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}