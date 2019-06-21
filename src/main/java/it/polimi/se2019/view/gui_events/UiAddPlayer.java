package it.polimi.se2019.view.gui_events;

import it.polimi.se2019.view.UiDispatcher;

public class UiAddPlayer extends UiEvent {
    private String player;

    public UiAddPlayer(String player){
        this.player = player;
    }

    @Override
    public void handle(UiDispatcher uiDispatcher) {
        uiDispatcher.dispatch(this);
    }

    public String getPlayer() {
        return player;
    }
}
