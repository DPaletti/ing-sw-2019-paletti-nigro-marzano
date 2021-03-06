package it.polimi.se2019.commons.vc_events;

import it.polimi.se2019.commons.utility.VCEventDispatcher;
import it.polimi.se2019.client.view.VCEvent;

/**
 * This event communicates the card the user decided to grab.
 * See {@link it.polimi.se2019.client.view.VCEvent}.
 */

public class GrabEvent extends VCEvent {
    private String grabbed;

    public GrabEvent(String source, String grabbed){
        super(source);
        this.grabbed=grabbed;
    }

    @Override
    public void handle(VCEventDispatcher dispatcher) {
        dispatcher.dispatch(this);
    }

    public String getGrabbed() {
        return grabbed;
    }
}
