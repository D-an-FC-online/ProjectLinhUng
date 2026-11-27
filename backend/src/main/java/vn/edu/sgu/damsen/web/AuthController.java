package vn.edu.sgu.damsen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.damsen.domain.AdminUser;
import vn.edu.sgu.damsen.repository.AdminUserRepository;
import vn.edu.sgu.damsen.service.ApiException;
import vn.edu.sgu.damsen.web.dto.Dtos.*;

/** Đăng nhập / đăng xuất quản trị (UC06). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final SecurityContextRepository contextRepository;
    private final AdminUserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager, SecurityContextRepository contextRepository,
                          AdminUserRepository users, PasswordEncoder encoder) {
        this.authManager = authManager;
        this.contextRepository = contextRepository;
        this.users = users;
        this.encoder = encoder;
    }

    /** Gọi trước khi đăng nhập để trình duyệt nhận cookie XSRF-TOKEN. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/login")
    public MeDto login(@Valid @RequestBody LoginRequest r, HttpServletRequest req, HttpServletResponse res) {
        Authentication auth;
        try {
            auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(r.username().trim(), r.password()));
        } catch (AuthenticationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu");
        }
        req.getSession(true);
        req.changeSessionId(); // chống session fixation
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        contextRepository.saveContext(ctx, req, res);
        return me(auth);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res, Authentication auth) {
        new SecurityContextLogoutHandler().logout(req, res, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeDto> current(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(me(auth));
    }

    @PostMapping("/change-password")
    @Transactional
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest r, Authentication auth) {
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }
        AdminUser u = users.findByUsername(auth.getName()).orElseThrow(() -> ApiException.notFound("tài khoản"));
        if (!encoder.matches(r.oldPassword(), u.getPasswordHash())) {
            throw ApiException.badRequest("Mật khẩu cũ không đúng");
        }
        u.setPasswordHash(encoder.encode(r.newPassword()));
        users.save(u);
        return ResponseEntity.noContent().build();
    }

    private static MeDto me(Authentication auth) {
        return new MeDto(auth.getName(), auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}
