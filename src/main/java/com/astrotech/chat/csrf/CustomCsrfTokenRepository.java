package com.astrotech.chat.csrf;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomCsrfTokenRepository implements CsrfTokenRepository {
    private final CookieCsrfTokenRepository delegate =
            CookieCsrfTokenRepository.withHttpOnlyFalse();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {

        var token =
                delegate.generateToken(request);

        System.out.println(
                "Generated CSRF: "
                        + token.getToken());

        return token;
    }

    @Override
    public void saveToken(
            CsrfToken token,
            HttpServletRequest request,
            HttpServletResponse response) {

        System.out.println(
                "Saving CSRF: "
                        + (token != null
                        ? token.getToken()
                        : "null"));

        delegate.saveToken(
                token,
                request,
                response);
    }

    @Override
    public CsrfToken loadToken(
            HttpServletRequest request) {

        var token =
                delegate.loadToken(request);

        System.out.println(
                "Loaded CSRF: "
                        + (token != null
                        ? token.getToken()
                        : "null"));

        return token;
    }
}