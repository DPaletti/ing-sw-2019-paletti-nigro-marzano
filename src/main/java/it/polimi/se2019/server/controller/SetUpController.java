package it.polimi.se2019.server.controller;

import it.polimi.se2019.client.view.VCEvent;
import it.polimi.se2019.commons.mv_events.*;
import it.polimi.se2019.commons.utility.BiSet;
import it.polimi.se2019.commons.utility.Log;
import it.polimi.se2019.commons.utility.Pair;
import it.polimi.se2019.commons.utility.Point;
import it.polimi.se2019.commons.vc_events.DisconnectionEvent;
import it.polimi.se2019.commons.vc_events.VcJoinEvent;
import it.polimi.se2019.commons.vc_events.VcMatchConfigurationEvent;
import it.polimi.se2019.commons.vc_events.VcReconnectionEvent;
import it.polimi.se2019.server.model.*;
import it.polimi.se2019.server.network.Server;

import java.util.*;

public class SetUpController extends Controller {
    private List<Integer> skulls = new ArrayList<>();
    private List<String> configs = new ArrayList<>();
    private List<Boolean> isFinalFrenzy = new ArrayList<>();
    private int counter = 0;
    private Random random = new Random();
    private TickingTimer setUpTimer = new TickingTimer(model, this::endTimer);

    public SetUpController(Game model, Server server, int roomNumber) {
        super(model, server, roomNumber);

        setUpTimer.startTimer(server.getMatchSetupTimer());
    }

    public SetUpController(Game model){
        this.model = model;
    }

    @Override
    public void update(VCEvent message)
    {
        if(disabled)
            return;
        try {
            message.handle(this);
        }catch (UnsupportedOperationException e){
            //this is the only controller registered on matchMaking thus it cannot receive unsupported events
            Log.fine("Received unsupported event " + message);
//            throw new UnsupportedOperationException("SetUpController: " + e.getMessage(), e);
        }
    }

    @Override
    public void dispatch(VcMatchConfigurationEvent message) {
        skulls.add(message.getSkulls());
        configs.add(message.getConf());
        isFinalFrenzy.add(message.isFrenzy());
        counter++;
        System.out.println(counter);
        if (counter == model.getUsernames().size()) {
            System.out.println("here");
            setUpTimer.endTimer();
        }

    }

    @Override
    public void dispatch(DisconnectionEvent message) {
        if(!message.isReconnection())
            model.send(new PausedPlayerEvent("*", message.getSource()));
    }

    @Override
    public void dispatch(VcReconnectionEvent message) {
        model.send(new UnpausedPlayerEvent("*", message.getUsername()));
    }

    @Override
    public void dispatch(VcJoinEvent message) {
        model.send(new MvJoinEvent("*", message.getUsername(), 0));
    }


    private void endTimer() {
        int skull = server.getDefaultSkulls();
        String config = "Large";
        boolean finalFrenzy = false;

        if (counter!=0) {
            skull = mostVoted(skulls);
            config = mostVoted(configs);
            finalFrenzy = mostVoted(isFinalFrenzy);
        }
        counter = 0;
        model.setGameMap(new GameMap(config));
        model.setKillshotTrack(new KillshotTrack(skull));
        model.setFinalFrenzy(finalFrenzy);

        Map<String, String> figureToUser = assignFigureToUser();
        Map<Point, String> lootTiles = lootTilesSetUp();
        Map<String, String> spawnTiles = weaponSpotsSetUp();

         new MatchController(model, server, model.getUsernames(), getRoomNumber());
         TurnController turnController = new TurnController(model, server, getRoomNumber());
         new WeaponController(server, getRoomNumber(), model);
         new PowerUpController(model, server, getRoomNumber());
         new DeathController(server, getRoomNumber(), model, turnController);


        model.send(new SetUpEvent("*", figureToUser,
                spawnTiles, lootTiles, skull, model.getGameMap().getConfig().getLeftHalf(),
                model.getGameMap().getConfig().getRightHalf(), finalFrenzy));
        startMatch();
        turnController.startTimer();
        this.disable();
     }

    private Map<String, String> weaponSpotsSetUp (){
        Map<String, String> weaponSpots= new HashMap<>();
        Grabbable drawnGrabbable;

        for (Tile t: model.getGameMap().getSpawnTiles()){
            for (int i=0; i<3; i++){
                drawnGrabbable= (Weapon)model.getWeaponDeck().draw();
                t.add(drawnGrabbable);
                weaponSpots.put(drawnGrabbable.getName(), t.getColour().name());
            }
        }
        return weaponSpots;
    }

    private Map<Point, String> lootTilesSetUp(){
        Map<Point, String> lootCards= new HashMap<>();
        Grabbable drawnGrabbable;

        for (Tile t: model.getGameMap().getLootTiles()){
            drawnGrabbable= (LootCard)model.getLootDeck().draw();
            t.add(drawnGrabbable);
            lootCards.put(t.getPosition(), drawnGrabbable.getName());
        }
        return lootCards;
    }

    private Map<String, String> assignFigureToUser (){
        HashMap<String, String> userToColour= new HashMap<>();
        int colourCounter=0;
        BiSet<FigureColour, String> lookup= new BiSet<>();
        List<Player> players= new ArrayList<>();
        for (String userCounter: model.getUsernames()){
            lookup.add(new Pair<>(FigureColour.values()[colourCounter], userCounter));
            players.add(new Player(new Figure(FigureColour.values()[colourCounter]), model));
            userToColour.put(userCounter, FigureColour.values()[colourCounter].name());
            colourCounter++;
        }
        model.setUserLookup(new BiSet<>(lookup));
        model.setPlayers(new ArrayList<>(players));
        return userToColour;
    }

     private <T> T mostVoted (List<T> objects){
        Map<T, Integer> votes= new HashMap<>();
        int maximum = -1;
        List<T> mostVoted= new ArrayList<>();
        for (T o: objects)
            votes.put(o, Collections.frequency(objects, o));
        for (Map.Entry<T, Integer> e: votes.entrySet()){
            if (maximum == -1 || e.getValue()>maximum){
                mostVoted.clear();
                mostVoted.add(e.getKey());
                maximum = e.getValue();
            }
            else
                mostVoted.add(e.getKey());
        }
        return mostVoted.get(random.nextInt(mostVoted.size()));
    }

    private void startMatch(){
        model.getPlayers().get(0).setFirstPowerUp((PowerUp)model.getPowerUpDeck().draw());
        model.getPlayers().get(0).setSecondPowerUp((PowerUp)model.getPowerUpDeck().draw());
        model.send(new StartFirstTurnEvent(model.playerToUser(model.getPlayers().get(0)),
                model.getPlayers().get(0).getFirstPowerUp().getName(),
                model.getPlayers().get(0).getSecondPowerUp().getName(),
                true, model.getGameMap().getMappedSpawnPoints()));
    }

    public void setConfigs(List<String> configs) {
        this.configs = configs;
    }

    public void setSkulls(List<Integer> skulls) {
        this.skulls = skulls;
    }

    public void setIsFinalFrenzy(List<Boolean> isFinalFrenzy) {
        this.isFinalFrenzy = isFinalFrenzy;
    }

}
