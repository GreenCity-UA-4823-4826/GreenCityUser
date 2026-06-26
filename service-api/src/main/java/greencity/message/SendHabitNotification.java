package greencity.message;

import greencity.constant.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Message, that is used for sending emails about not marked habits.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SendHabitNotification implements Serializable {
    @NotBlank
    private String name;

    @Email(message = ValidationConstants.INVALID_EMAIL)
    @NotBlank
    private String email;
}
