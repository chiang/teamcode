package io.teamcode.config;

import io.teamcode.common.ci.statemachine.PipelineEvent;
import io.teamcode.domain.entity.ci.PipelineStatus;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chiang on 2017. 4. 18..
 */
public class StateMachineEventListener extends StateMachineListenerAdapter<PipelineStatus, PipelineEvent> {

    private final LinkedList<String> messages = new LinkedList<String>();

    public List<String> getMessages() {
        return messages;
    }

    public void resetMessages() {
        messages.clear();
    }

    @Override
    public void stateContext(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        if (stateContext.getStage() == StateContext.Stage.STATE_ENTRY) {
            messages.addFirst(stateContext.getStateMachine().getId() + " enter " + stateContext.getTarget().getId());
        } else if (stateContext.getStage() == StateContext.Stage.STATE_EXIT) {
            messages.addFirst(stateContext.getStateMachine().getId() + " exit " + stateContext.getSource().getId());
        }
    }


}
