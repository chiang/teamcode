package io.teamcode.common.ci.statemachine;

/**
 * Created by chiang on 2017. 4. 13..
 */
public enum PipelineEvent {

    ENQUEUE, RUN, SKIP, SUCCESS, CANCEL, DROP, RETRY,

    //Manual 인 것을 실행할 때
    PLAY
}
