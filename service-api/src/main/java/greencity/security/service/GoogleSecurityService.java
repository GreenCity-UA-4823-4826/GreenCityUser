package greencity.security.service;

import greencity.security.dto.SuccessSignInDto;

public interface GoogleSecurityService {
    SuccessSignInDto signIn(String idToken);
}
