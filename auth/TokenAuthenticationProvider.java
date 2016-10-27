package com.spfsolutions.ioms.auth;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.j256.ormlite.dao.Dao;
import com.spfsolutions.ioms.common.ClientToken;
import com.spfsolutions.ioms.data.MenuEntity;
import com.spfsolutions.ioms.utils.ClientTokenDecrypter;

public class TokenAuthenticationProvider implements AuthenticationProvider{
    
    
    @Override
    public Authentication authenticate(Authentication auth)
            throws AuthenticationException {
        if (auth.isAuthenticated())
            return auth;

        String token = auth.getCredentials().toString();
        ClientToken clientToken = new ClientToken(token, DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC).plusMinutes(15));
        String encryptedToken = ClientTokenDecrypter.encrypt(clientToken);         
        ClientToken decryptedToken = ClientTokenDecrypter.decrypt(encryptedToken);
        
        if (decryptedToken == null || decryptedToken.getExpirationDate().isBefore(DateTime.now(DateTimeZone.UTC))) {
            throw new BadCredentialsException("Invalid token " + token);
            
        }
        auth = new PreAuthenticatedAuthenticationToken(clientToken, token);
        auth.setAuthenticated(true);
            
        return auth;
    }
    @Override
    public boolean supports(Class<?> authentication) {
        // TODO Auto-generated method stub
        return false;
    }

}
