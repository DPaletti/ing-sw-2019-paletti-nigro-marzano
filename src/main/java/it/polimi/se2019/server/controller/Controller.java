package it.polimi.se2019.server.controller;

import it.polimi.se2019.server.model.AmmoColour;
import it.polimi.se2019.server.model.Game;
import it.polimi.se2019.server.model.Player;
import it.polimi.se2019.server.model.PowerUp;
import it.polimi.se2019.commons.mv_events.DisablePowerUpEvent;
import it.polimi.se2019.server.network.Server;
import it.polimi.se2019.commons.utility.Observer;
import it.polimi.se2019.commons.utility.VCEventDispatcher;
import it.polimi.se2019.client.view.VCEvent;

public abstract class Controller implements Observer<VCEvent>, VCEventDispatcher {
    //TODO evaluate the need for storing a reference to the model, probably needed
    protected Game model;
    protected Server server;
    private int roomNumber;

    public Controller(Game model, Server server, int roomNumber){
        this.model = model;
        this.server = server;
        this.roomNumber = roomNumber;
        server.addController(this,roomNumber);
    }

    public Controller (){
        //empty constructor
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public boolean enoughActivePlayers (){
        int active = 0;
        for (Player p : model.getPlayers()) {
            if(!p.isPaused())
                active++;
        }
        return !(active < 3);
    }

    public String getNextActiveUser (String user){
        if (model.getUsernames().indexOf(user) + 1 >= model.getUsernames().size())
            return getNextActiveUser(model.getUsernames().get(0));

        if (model.userToPlayer(model.getUsernames().get(model.getUsernames().indexOf(user) + 1)).isPaused())
            return getNextActiveUser(model.getUsernames().get(model.getUsernames().indexOf(user) + 1));
        return model.getUsernames().get(model.getUsernames().indexOf(user) + 1);
    }

    protected void disablePowerUps(String currentPlayer, String constraint){
        for (PowerUp p : model.userToPlayer(currentPlayer).getPowerUps()) {
            if (p.getConstraint().equalsIgnoreCase(constraint))
                model.send(new DisablePowerUpEvent(currentPlayer, p.getName()));
        }
    }

    protected AmmoColour stringToAmmo (String ammoName){
        for (AmmoColour ammoColour: AmmoColour.values()){
            if (ammoColour.toString().equalsIgnoreCase(ammoName)){
                return ammoColour;
            }
        }
        throw new NullPointerException("This ammo doesn't exist: " + ammoName);
    }
}