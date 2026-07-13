package com.astrotech.chat.websocket;

import com.astrotech.chat.dto.request.AuthenticatedUser;

import java.security.Principal;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import lombok.experimental.UtilityClass;
@UtilityClass
public class WebSocketUtils {

    public AuthenticatedUser extractUserFromHeader(Principal principal) {

        if (principal instanceof UsernamePasswordAuthenticationToken authentication &&
                authentication.getPrincipal() instanceof AuthenticatedUser user) {

            return user;
        }

        return null;
    }


}
