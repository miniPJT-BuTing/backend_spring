package com.mini.buting.global.security.service;

import com.mini.buting.global.security.principal.AuthUser;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenAuthService {
    public void issueJwt(AuthUser authUser, HttpServletResponse response);
}
