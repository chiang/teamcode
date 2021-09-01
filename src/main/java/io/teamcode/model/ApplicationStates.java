package io.teamcode.model;

import lombok.Data;
import org.springframework.stereotype.Component;

/***
 * 초기화된 상태 값을 기준으로 Application 상태를 저장합니다.
 *
 */
@Data
@Component
public class ApplicationStates {

    private ApplicationState smtpState;

}
