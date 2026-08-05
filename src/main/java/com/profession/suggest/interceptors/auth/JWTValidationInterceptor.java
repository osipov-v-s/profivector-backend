package com.profession.suggest.interceptors.auth;

import com.profession.suggest.database.repositories.applicant.ApplicantRepository;
import com.profession.suggest.services.jwt.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class JWTValidationInterceptor implements HandlerInterceptor {
    private final JWTService jwtService;
    private final ApplicantRepository applicantRepository;

    public JWTValidationInterceptor(JWTService jwtService, ApplicantRepository applicantRepository) {
        this.jwtService = jwtService;
        this.applicantRepository = applicantRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            //response.setStatus(HttpServletResponse.SC_OK);
            return true; // Do not proceed with further processing
        }
        if ("GET".equalsIgnoreCase(request.getMethod())
                && "/api/specialists/professions".equals(request.getRequestURI())) {
            return true;
        }

        try {
            String jwtToken = request.getHeader("Authorization");
            if (jwtToken != null){
                String token = jwtToken.replace("Bearer ", "");
                if (!jwtService.isValid(token)){
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token or credentials");
                    return false;
                }

                Long accountId = Long.valueOf(jwtService.extractSubject(token));
                if (applicantRepository.findByAccountId(accountId)
                        .map(applicant -> !applicant.isActive())
                        .orElse(false)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Inactive account");
                    return false;
                }
                request.setAttribute("accountId", accountId);

                JWTAuth jwtAuth = new JWTAuth(jwtToken);
                SecurityContextHolder.getContext().setAuthentication(jwtAuth);
                return true;
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token or credentials");
                return false;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }
    }
}
