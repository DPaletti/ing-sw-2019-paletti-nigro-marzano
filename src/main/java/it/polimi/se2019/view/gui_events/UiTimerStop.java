package it.polimi.se2019.view.gui_events;

import it.polimi.se2019.view.UiDispatcher;

public class UiTimerStop extends UiEvent {
    @Override
    public void handle(UiDispatcher uiDispatcher) {
        uiDispatcher.dispatch(this);
    }
}
