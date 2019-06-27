package it.polimi.se2019.view.ui_events;

import it.polimi.se2019.view.UiDispatcher;

public class UiRemovePlayer extends UiEvent {
    private String player;

    public UiRemovePlayer(String username){
        this.player = username;
    }

    public String getPlayer() {
        return player;
    }

    @Override
    public void handle(UiDispatcher uiDispatcher) {
        uiDispatcher.dispatch(this);
    }
}