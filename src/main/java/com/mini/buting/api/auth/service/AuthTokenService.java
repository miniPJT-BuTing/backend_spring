package com.mini.buting.api.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthTokenService {
    void logout(HttpServletRequest request, HttpServletResponse response);
}
