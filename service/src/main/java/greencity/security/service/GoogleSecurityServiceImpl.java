package greencity.security.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import greencity.constant.AppConstant;
import greencity.dto.user.UserVO;
import greencity.entity.Language;
import greencity.entity.User;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.BadUserStatusException;
import greencity.repository.UserRepo;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.jwt.JwtTool;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleSecurityServiceImpl implements GoogleSecurityService {
    private static final long ENGLISH_LANGUAGE_ID = 2L;

    private final UserRepo userRepo;
    private final JwtTool jwtTool;
    private final GoogleIdTokenVerifier googleIdTokenVerifier =
        new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance()).build();

    @Value("${google.clientId}")
    private String googleClientId;

    @Value("${google.clientId.manager:}")
    private String googleClientIdManager;

    @Override
    @Transactional
    public SuccessSignInDto signIn(String idToken) {
        GoogleIdToken.Payload payload = verifyToken(idToken);
        String email = payload.getEmail();

        User user = userRepo.findByEmail(email)
            .map(this::prepareExistingUser)
            .orElseGet(() -> createGoogleUser(payload));

        User savedUser = userRepo.save(user);

        String accessToken = jwtTool.createAccessToken(savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtTool.createRefreshToken(toUserVO(savedUser));
        return new SuccessSignInDto(savedUser.getId(), accessToken, refreshToken, savedUser.getName(), true);
    }

    private GoogleIdToken.Payload verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Google idToken is required");
        }

        try {
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                throw new IllegalArgumentException("Google idToken is invalid");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            if (!isExpectedAudience(payload.getAudience())) {
                throw new IllegalArgumentException("Google idToken audience is invalid");
            }
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new IllegalArgumentException("Google email is not verified");
            }
            return payload;
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Google idToken cannot be verified", e);
        }
    }

    private boolean isExpectedAudience(Object audience) {
        return Objects.equals(audience, googleClientId)
            || Objects.equals(audience, googleClientIdManager);
    }

    private User prepareExistingUser(User user) {
        if (user.getUserStatus() == UserStatus.BLOCKED) {
            throw new BadUserStatusException("User is blocked");
        }
        if (user.getUserStatus() == UserStatus.DEACTIVATED) {
            throw new BadUserStatusException("User is deactivated");
        }

        user.setUserStatus(UserStatus.ACTIVATED);
        user.setLastActivityTime(LocalDateTime.now());
        if (user.getRefreshTokenKey() == null || user.getRefreshTokenKey().isBlank()) {
            user.setRefreshTokenKey(jwtTool.generateTokenKey());
        }
        if (user.getLanguage() == null) {
            user.setLanguage(defaultLanguage());
        }
        return user;
    }

    private User createGoogleUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String name = getName(payload);

        return User.builder()
            .name(name)
            .firstName(name)
            .email(email)
            .dateOfRegistration(LocalDateTime.now())
            .role(Role.ROLE_USER)
            .refreshTokenKey(jwtTool.generateTokenKey())
            .lastActivityTime(LocalDateTime.now())
            .userStatus(UserStatus.ACTIVATED)
            .emailNotification(EmailNotification.DISABLED)
            .rating(AppConstant.DEFAULT_RATING)
            .uuid(UUID.randomUUID().toString())
            .showLocation(true)
            .showEcoPlace(true)
            .showShoppingList(true)
            .language(defaultLanguage())
            .build();
    }

    private String getName(GoogleIdToken.Payload payload) {
        Object name = payload.get("name");
        if (name instanceof String value && !value.isBlank()) {
            return trimToUserNameLength(value);
        }
        return trimToUserNameLength(payload.getEmail().substring(0, payload.getEmail().indexOf('@')));
    }

    private String trimToUserNameLength(String value) {
        return value.length() > 30 ? value.substring(0, 30) : value;
    }

    private Language defaultLanguage() {
        return Language.builder()
            .id(ENGLISH_LANGUAGE_ID)
            .code(AppConstant.DEFAULT_LANGUAGE_CODE)
            .build();
    }

    private UserVO toUserVO(User user) {
        return UserVO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .refreshTokenKey(user.getRefreshTokenKey())
            .build();
    }
}
