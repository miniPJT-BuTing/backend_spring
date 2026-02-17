package com.mini.buting.api.auth.service;

import com.mini.buting.global.security.principal.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthTokenService {
    public void logout(HttpServletRequest request, HttpServletResponse response);

    public void issueJwt(AuthUser authUser, HttpServletResponse response);
}
