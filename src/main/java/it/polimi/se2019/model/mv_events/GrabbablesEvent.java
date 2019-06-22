package it.polimi.se2019.model.mv_events;

import it.polimi.se2019.model.Grabbable;
import it.polimi.se2019.utility.MVEventDispatcher;
import it.polimi.se2019.view.MVEvent;

import java.util.ArrayList;
import java.util.List;

public class GrabbablesEvent extends MVEvent {
    private ArrayList<String> grabbables;

    public GrabbablesEvent(String destination, List<String> grabbables) {
        super(destination);
        this.grabbables = new ArrayList<>(grabbables);
    }

    @Override
    public void handle(MVEventDispatcher dispatcher) {
        dispatcher.dispatch(this);
    }

    public List<String> getGrabbables() {
        return grabbables;
    }
}