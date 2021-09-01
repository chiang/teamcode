package io.teamcode.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//TODO Error 메시지를 reason 으로 처리할 수 있도록...
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Cannot commit excepiton.")
public class CannotCommitPipelineYamlException extends RuntimeException {

    public CannotCommitPipelineYamlException(Throwable t) {
        super(t);
    }
}
