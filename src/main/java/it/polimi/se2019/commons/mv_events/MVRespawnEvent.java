package it.polimi.se2019.commons.mv_events;

import it.polimi.se2019.commons.utility.MVEventDispatcher;
import it.polimi.se2019.client.view.MVEvent;

/**
 * This event is sent to a dead player when they have to respawn and a power up card is drawn for them.
 * See {@link it.polimi.se2019.client.view.MVEvent}.
 */

public class MVRespawnEvent extends MVEvent {
    private String drawnPowerUpName;

    public MVRespawnEvent(String destination, String drawnPowerUpName) {
        super(destination);
        this.drawnPowerUpName = drawnPowerUpName;
    }

    public String getDrawnPowerUpName() {
        return drawnPowerUpName;
    }

    @Override
    public void handle(MVEventDispatcher dispatcher) {
        dispatcher.dispatch(this);
    }
}
