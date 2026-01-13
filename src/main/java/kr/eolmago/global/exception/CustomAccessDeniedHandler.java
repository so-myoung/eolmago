package kr.eolmago.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.eolmago.dto.api.user.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String requestUri = request.getRequestURI();

        // API 요청인 경우 JSON 응답
        if (requestUri.startsWith("/api/")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    ErrorResponse.of(403, "Forbidden", "접근 권한이 없습니다.")
            ));
        } else {
            // 웹 페이지 요청인 경우 알림창 띄우고 이전 페이지로 이동
            response.setContentType("text/html; charset=UTF-8");
            response.setStatus(HttpStatus.OK.value()); // 브라우저가 스크립트를 정상적으로 실행하도록 200 OK로 설정
            PrintWriter out = response.getWriter();
            out.println("<script>alert('접근 권한이 없습니다.'); history.back();</script>");
            out.flush();
        }
    }
}
