package com.playground.security.handler;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

@Log4j2
public class CustomFailureHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response, AccessDeniedException accessException)
            throws IOException, ServletException {

        log.error("Access Denied Handler");

        log.error("Redirect....");

        response.sendRedirect("/login");
        //response.sendRedirect("/error"); // error페이지로 이동

    }

}
