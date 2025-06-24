package com.ilich.sb.e_commerce.service;

import java.util.Optional;

import com.ilich.sb.e_commerce.model.RefreshToken;
import com.ilich.sb.e_commerce.model.User;

public interface IRefreshTokenService {

    RefreshToken createRefreshToken(User user);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    int deleteByUserId(Long userId);

    void cleanExpiredRefreshTokens();

}
