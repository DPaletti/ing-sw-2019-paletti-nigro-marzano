package it.polimi.se2019.client.view.ui_events;

import it.polimi.se2019.client.view.UiDispatcher;

public class UiGrabbedLoot extends UiEvent {
    private String grabbed;

    public UiGrabbedLoot(String grabbed) {
        this.grabbed = grabbed;
    }

    public String getGrabbed() {
        return grabbed;
    }

    @Override
    public void handle(UiDispatcher uiDispatcher) {
        uiDispatcher.dispatch(this);
    }
}
