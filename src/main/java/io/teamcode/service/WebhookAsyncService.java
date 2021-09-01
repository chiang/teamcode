package io.teamcode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 *
 * TODO, queuing, timeout
 *
 * Created by chiang on 2017. 1. 21..
 */
@Component
public class WebhookAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookAsyncService.class);

    @Async
    public void send() {

        logger.info("훅을 전달했습니다.");
    }

}
