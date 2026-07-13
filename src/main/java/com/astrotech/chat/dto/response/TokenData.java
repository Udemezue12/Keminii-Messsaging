package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.JwtType;
import com.astrotech.chat.enums.UserRole;
import java.util.Date;

public record TokenData(String id,
                        String userId,
                        Date expiration,
                        boolean isToken,
                        UserRole role,
                        JwtType jwtType,
                        boolean isExpired,
                        String sessionId,
                        boolean emailVerified,
                        String displayName) {}
