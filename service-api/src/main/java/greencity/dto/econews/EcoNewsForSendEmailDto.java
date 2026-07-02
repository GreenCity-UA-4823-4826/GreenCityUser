package greencity.dto.econews;

import greencity.dto.user.PlaceAuthorDto;
import java.time.ZonedDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class EcoNewsForSendEmailDto {
    private String unsubscribeToken;

    private ZonedDateTime creationDate;

    private String imagePath;

    private String source;

    @Valid
    @NotNull
    private PlaceAuthorDto author;

    private String title;

    private String text;
}
