package it.polimi.se2019.model.mv_events;

import it.polimi.se2019.utility.Point;
import it.polimi.se2019.utility.MVEventDispatcher;
import it.polimi.se2019.view.MVEvent;

import java.util.ArrayList;
import java.util.List;

public class AllowedMovementsEvent extends MVEvent {
    private ArrayList<Point> allowedPositions;
    private String userToMove;

    public AllowedMovementsEvent(String destination, List<Point> allowedPositions, String userToMove){
        super(destination);
        this.allowedPositions= new ArrayList<>(allowedPositions);
        this.userToMove = userToMove;
    }

    public List<Point> getAllowedPositions() {
        return new ArrayList<>(allowedPositions);
    }

    public String getUserToMove() {
        return userToMove;
    }

    @Override
    public void handle(MVEventDispatcher dispatcher) {
        dispatcher.dispatch(this);
    }
}
