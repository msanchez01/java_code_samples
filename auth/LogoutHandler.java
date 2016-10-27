package com.spfsolutions.ioms.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

public class LogoutHandler extends SimpleUrlLogoutSuccessHandler  {
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		setDefaultTargetUrl("/");		
		request.getSession().invalidate();
		super.onLogoutSuccess(request, response, authentication);
	}

}
