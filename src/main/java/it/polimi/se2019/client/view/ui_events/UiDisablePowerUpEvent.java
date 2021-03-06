package it.polimi.se2019.client.view.ui_events;

import it.polimi.se2019.client.view.UiDispatcher;

public class UiDisablePowerUpEvent extends UiEvent {
    private String toDisable;


    public UiDisablePowerUpEvent(String toDisable) {
        this.toDisable = toDisable;
    }

    public String getToDisable() {
        return toDisable;
    }

    @Override
    public void handle(UiDispatcher uiDispatcher) {
        uiDispatcher.dispatch(this);
    }
}
