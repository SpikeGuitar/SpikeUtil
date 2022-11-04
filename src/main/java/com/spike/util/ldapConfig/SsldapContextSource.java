package com.spike.util.ldapConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.Context;
import java.util.Hashtable;

@Slf4j
public class SsldapContextSource  extends LdapContextSource {
    @Override
    public Hashtable<String, Object> getAnonymousEnv(){
        System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
        System.setProperty("java.naming.ldap.attributes.binary", "objectGUID");
        Hashtable<String, Object> anonymousEnv = super.getAnonymousEnv();
        anonymousEnv.put("java.naming.security.protocol", "ssl");
        anonymousEnv.put("java.naming.ldap.factory.socket", CustomSslSocketFactory.class.getName());
        anonymousEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        anonymousEnv.put("java.naming.ldap.attributes.binary", "objectGUID");
        return anonymousEnv;
    }
}
