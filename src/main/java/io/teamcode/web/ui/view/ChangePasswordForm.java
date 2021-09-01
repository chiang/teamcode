package io.teamcode.web.ui.view;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by chiang on 2017. 2. 3..
 */
@Data
public class ChangePasswordForm {

    private String userName;

    @NotBlank(message = "First Name is required")
    private String currentPassword;

    private String password;

    private String passwordConfirmation;
}
