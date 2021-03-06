package it.polimi.se2019.client.view.ui_events;

import it.polimi.se2019.client.view.UiDispatcher;

public class UiTimerStart extends UiEvent {
    private int duration;

    public UiTimerStart(int duration){
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public void handle(UiDispatcher uiDispatcher) {
        uiDispatcher.dispatch(this);
    }
}
