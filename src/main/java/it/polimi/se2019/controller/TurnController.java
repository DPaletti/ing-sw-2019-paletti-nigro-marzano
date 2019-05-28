package it.polimi.se2019.controller;

import it.polimi.se2019.model.*;
import it.polimi.se2019.network.Server;
import it.polimi.se2019.utility.JsonHandler;
import it.polimi.se2019.utility.Log;
import it.polimi.se2019.utility.VCEventDispatcher;
import it.polimi.se2019.view.VCEvent;
import it.polimi.se2019.view.vc_events.*;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class TurnController extends Controller {
    private Dispatcher dispatcher= new Dispatcher();
    private Semaphore sem = new Semaphore(1, true);
    private ArrayList<String> effects= new ArrayList<>();
    private ArrayList<ArrayList<String>> targets= new ArrayList<>();

    public class TargetListener implements Runnable{
    private Dispatcher dispatcher = new Dispatcher(); //care not to leave this uninitialized

        @Override
        public void run() {
            try {
                sem.acquire();
            } catch (InterruptedException e){
                Log.severe(e.getMessage());
                Thread.currentThread().interrupt();
            }

        }
    }
    public TurnController (Game model, Server server, int roomNumber){
        super(model, server, roomNumber);
        //turn controller is registered to virtualView in closeMatchMaking() inside MatchMaking controller
        //either leave things like this or take that one out and add server.addController(this) here
    }

    @Override
    public void update(VCEvent message) {
        try {
            message.handle(dispatcher);
        }catch (UnsupportedOperationException e){
            //ignore events that this controller does not support
            Log.fine("TurnController ignored " + JsonHandler.serialize(message));
        }
    }

    private class Dispatcher extends VCEventDispatcher{
        @Override
        public void update(DefineTeleportPositionEvent message) {
            model.teleportPlayer(message.getSource(), message.getTeleportPosition());
        }

        @Override
        public void update(ReloadEvent message) {
            model.reloadWeapon(message.getSource(), message.getWeaponName());
        }

        @Override
        public void update(MoveEvent message) {
            model.run (message.getSource(), message.getDestionation());
        }

        @Override
        public void update(GrabEvent message) {
            model.grab(message.getSource());
        }

        @Override
        public void update(ChosenTargetAndEffectEvent message) {
            effects= message.getEffectNames();
            targets= message.getTargetNames();
            sem.release();
        }

        @Override
        public void update(ChosenWeaponEvent message) {
            ArrayList<String> effect= new ArrayList<>(); //acquired from ChosenEffectEvent
            ArrayList<ArrayList<String>> target= new ArrayList<>(); //acquired from ChosenTargetAndEffectEvent
            Thread listener = new Thread(new TargetListener());
            listener.start();
            try {
                sem.acquire();
            } catch (InterruptedException e){
                Log.severe(e.getMessage());
                Thread.currentThread().interrupt();
            }
            model.shoot(message.getSource(), message.getWeapon(), effect, target);
        }

        @Override
        public void update(SpawnEvent message) {
            try {
                model.spawn(message.getSource(), stringToAmmo(message.getDiscardedPowerUp()));
            } catch (NullPointerException e) {
                Log.severe(e.getMessage());
            }
            model.startTurn(message.getSource());
        }

        @Override
        public void update(PowerUpUsageEvent message) {
            try {
                model.usePowerUp(message.getSource(), message.getUsedPowerUp(), stringToAmmo(message.getPowerUpColour()));
            } catch (NullPointerException e){
                Log.severe(e.getMessage());
            }
        }

        @Override
        public void update(ActionEvent message) {
            if (message.getAction().equalsIgnoreCase("runAround")) model.allowedMovements(message.getSource(), 3);
            else if (message.getAction().equalsIgnoreCase("moveAndGrab")) model.allowedMovements(message.getSource(), 1);
            else if (message.getAction().equalsIgnoreCase("shootPeople")) model.allowedWeapons(message.getSource());
            else if (message.getAction().equalsIgnoreCase("moveMoveGrab")) model.allowedMovements(message.getSource(), 2);
            else if (message.getAction().equalsIgnoreCase("moveAndShoot")) model.allowedMovements(message.getSource(), 1);
            else if (message.getAction().equalsIgnoreCase("frenzyMoveReloadShoot")) model.allowedMovements(message.getSource(), 1);
            else if (message.getAction().equalsIgnoreCase("moveFourSquares")) model.allowedMovements(message.getSource(), 4);
            else if (message.getAction().equalsIgnoreCase("frenzyMoveAndGrab")) model.allowedMovements(message.getSource(), 1);
            else if (message.getAction().equalsIgnoreCase("moveTwiceGrabShoot")) model.allowedMovements(message.getSource(), 2);
            else if (message.getAction().equalsIgnoreCase("moveThriceAndShoot")) model.allowedMovements(message.getSource(), 3);
        }

        private AmmoColour stringToAmmo (String ammoName){
            for (AmmoColour ammoColour: AmmoColour.values()){
                if (ammoColour.toString().equalsIgnoreCase(ammoName)){
                    return ammoColour;
                }
            }
            throw new NullPointerException("This ammo doesn't exist");
        }
    }

}
