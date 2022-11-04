package com.spike.util.ldapConfig;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.Objects;

@Slf4j
public class LdapConfiguration {

    private LdapTemplate ldapTemplate;

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String ldapBaseDc;

    @Value("${spring.ldap.username}")
    private String ldapUsername;

    @Value("${spring.ldap.password}")
    private String ldapPasswd;


    @Bean
    public LdapContextSource contextSource() {

        SsldapContextSource ldapContextSource = new SsldapContextSource();
        log.info("url：{}", ldapUrl);
        ldapContextSource.setBase(ldapBaseDc);
        ldapContextSource.setUrl(ldapUrl);
        ldapContextSource.setUserDn(ldapUsername);
        ldapContextSource.setPassword(ldapPasswd);
        ldapContextSource.setPooled(false);
        ldapContextSource.setReferral("follow");
        ldapContextSource.afterPropertiesSet();
        return ldapContextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        if (Objects.isNull(contextSource)) {
            throw new RuntimeException("ldap contextSource error");
        }
        if (null == ldapTemplate) {
            ldapTemplate = new LdapTemplate(contextSource);
        }
        return ldapTemplate;
    }

}
