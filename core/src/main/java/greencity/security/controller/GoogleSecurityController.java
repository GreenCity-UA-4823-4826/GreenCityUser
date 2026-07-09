package greencity.security.controller;

import greencity.security.dto.SuccessSignInDto;
import greencity.security.service.GoogleSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GoogleSecurityController {
    private final GoogleSecurityService googleSecurityService;

    @GetMapping("/googleSecurity")
    public ResponseEntity<SuccessSignInDto> signIn(@RequestParam String idToken) {
        return ResponseEntity.ok(googleSecurityService.signIn(idToken));
    }
}
