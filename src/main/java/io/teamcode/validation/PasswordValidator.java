package io.teamcode.validation;

import io.teamcode.web.ui.view.ChangePasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created by chiang on 2017. 2. 3..
 */
@Component
public class PasswordValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangePasswordForm customer = (ChangePasswordForm)target;
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "customer.password.empty");
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confPassword", "customer.confPassword.empty");
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "age", "customer.age.empty");

        //Business validation
        if(!customer.getPassword().equals(customer.getPasswordConfirmation())){
            //FIXME 동작이 되나 ? errors.rejectValue("passwordConfirmation", "profile.password.missMatch");
            errors.rejectValue("passwordConfirmation", "재확인 비밀번호가 맞지 않습니다.");
        }
        if(customer.getPassword().length() < 8) {
            //errors.rejectValue("password", "profile.password.tooShort");
            errors.rejectValue("passwordConfirmation", "비밀번호는 8자 이상으로 입력해 주세요.");
        }
        if (customer.getCurrentPassword().equals(customer.getPassword())) {
            errors.rejectValue("passwordConfirmation", "이전 비밀번호를 사용하실 수 없습니다. 다른 비밀번호를 입력해 주세요.");
        }
        if (!customer.getPassword().matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
            errors.rejectValue("passwordConfirmation", "영문, 숫자, 특수문자 조합으로 입력해 주세요.");
        }

    }
}
