package com.spfsolutions.ioms.auth;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.j256.ormlite.dao.Dao;
import com.spfsolutions.ioms.data.RoleEntity;
import com.spfsolutions.ioms.data.RolePermissionEntity;
import com.spfsolutions.ioms.data.UserEntity;
import com.spfsolutions.ioms.data.UserRoleEntity;
import com.spfsolutions.ioms.utils.MD5;


public class UserAuthenticationProvider implements AuthenticationProvider {
     
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String PERMISSION_PREFIX = "PERMISSION_";
    
    @Autowired
    Dao<UserEntity, Integer> userDao;
        
    public UserAuthenticationProvider(){super();}
 
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            UserEntity userEntity = userDao.queryForFirst(userDao.queryBuilder().where().eq("Username", authentication.getName()).prepare());
            
            String inputHash = MD5.encrypt(authentication.getCredentials().toString());
            if(userEntity == null || !userEntity.getPassword().equals(inputHash)){
                throw new BadCredentialsException("Username or password incorrect.");                
            }
            else if (!userEntity.isEnabled())
            {
                throw new DisabledException("The username is disabled. Please contact your System Administrator.");
            }
            userEntity.setLastSuccessfulLogon(new DateTime(DateTimeZone.UTC).toDate());
            
            userDao.createOrUpdate(userEntity);
            
            Collection<SimpleGrantedAuthority> authorities = buildRolesFromUser(userEntity);
            UsernamePasswordAuthenticationToken token = new 
                    UsernamePasswordAuthenticationToken(authentication.getName(), authentication.getCredentials(), authorities);
        
            return token;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            userDao.getConnectionSource().closeQuietly();
        }
        return null;
 
    }
    private Collection<SimpleGrantedAuthority> buildRolesFromUser(UserEntity user) {
        Collection<SimpleGrantedAuthority> authorities = new HashSet<SimpleGrantedAuthority>();
        
        Iterator<UserRoleEntity> userRolesIterator = user.getUserroleassignments().iterator();
        
        while(userRolesIterator.hasNext())
        {
            RoleEntity role = userRolesIterator.next().getRoles();
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getRoleName()));
            
            Iterator<RolePermissionEntity> rolePermissionsIterator = role.getRolePermissions().iterator();
            while(rolePermissionsIterator.hasNext())
            {
                RolePermissionEntity rolePermissionEntity = rolePermissionsIterator.next();
                String permissionName = rolePermissionEntity.getPermission().getPermissionName();
                authorities.add(new SimpleGrantedAuthority(PERMISSION_PREFIX + permissionName));
            }
            
        }
        return authorities;
    }
 
    @Override
    public boolean supports(Class authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
 
    /*public UserService getUserService() {
        return userService;
    }
 
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
 
    public LicenseInformationWrapper getLicenseInformationWrapper() {
        return licenseInformationWrapper;
    }
 
    public void setLicenseInformationWrapper(LicenseInformationWrapper licenseInformationWrapper) {
        this.licenseInformationWrapper = licenseInformationWrapper;
    }*/
 
}
