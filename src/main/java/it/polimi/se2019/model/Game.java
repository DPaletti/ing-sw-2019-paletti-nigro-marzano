package it.polimi.se2019.model;

import it.polimi.se2019.model.mv_events.*;
import it.polimi.se2019.utility.*;
import it.polimi.se2019.utility.Observable;
import it.polimi.se2019.view.MVEvent;

import java.util.*;

public class Game extends Observable<MVEvent> {
    private GameMap gameMap;
    private boolean finalFrenzy= true;
    private KillshotTrack killshotTrack;
    private Deck weaponDeck;
    private Deck powerUpDeck;
    private Deck lootDeck;
    private List<Player> players= new ArrayList<>();
    private BiSet<FigureColour, String> userLookup = new BiSet<>();
    private Random randomConfig= new Random();
    private TurnMemory turnMemory = new TurnMemory();
    private WeaponHelper weaponHelper=new WeaponHelper();
    private ComboHelper comboHelper=new ComboHelper();
    private PowerUpHelper powerUpHelper=new PowerUpHelper();
    private List <Integer> pointsToAssign= new ArrayList<>(Arrays.asList(8, 6, 4, 2, 1, 1, 1, 1));
    private List <Integer> frenzyPointsToAssign= new ArrayList<>(Arrays.asList(2, 1, 1, 1, 1));

    private MVSelectionEvent selectionEventHolder = null;

    // TODO Mapping between figures and usernames coming from controller

    //TODO lootCard Helper, json of lootCards
    public Game(){
        weaponDeck= new Deck(new ArrayList<>(weaponHelper.getWeapons()));
        powerUpDeck= new Deck(new ArrayList<>(powerUpHelper.getPowerUps()));
        /*lootDeck= new Deck(new ArrayList<>(CardHelper.getInstance().getAllLootCards()));*/
        observers = new ArrayList<>();
    }

    public void weaponEnd (String playing){
        notify(new MVWeaponEndEvent(playing));
    }

    public void apply(String playing, List<Player> players,List<Action> actions){
        for (Action action:actions){
            for (Player player: players){
                //TODO Put action method in action enumeration
            }
        }
    }

    public void sendPossibleEffects (String username, String weaponName, List<GraphWeaponEffect> weaponEffect){
        PossibleEffectsEvent event = new PossibleEffectsEvent(username, weaponName);
        for (GraphWeaponEffect w: weaponEffect){
            event.addEffect(w.getName(), w.getEffectType());
        }
        notify(event);
    }

    public void addToSelection(String playerSelecting, List<Action> actions, List<Targetable> targetables){
        if(selectionEventHolder == (null))
            selectionEventHolder = new MVSelectionEvent(playerSelecting);
        if(!targetables.isEmpty())
            targetables.get(0).addToSelectionEvent(selectionEventHolder, targetables, actions);

    }

    public void sendPossibleTargets (){
        notify(selectionEventHolder);
        selectionEventHolder = null;
    }

    public void timerTick(int timeToGo){

        notify(new TimerEvent("*", timeToGo));
    }

    public void sendPartialEffectConflict (String username, List<ArrayList<Action>> possibleActions, List<ArrayList<String>>previousTargets){
        notify(new PartialEffectEvent(username, possibleActions, previousTargets));
    }

    public void newPlayerInMatchMaking(String token, String username){
        notify(new MvJoinEvent(token, username));
    }

    public void playerReconnection(String token, String oldToken, boolean isMatchMaking){
        notify(new MvReconnectionEvent(token, oldToken, isMatchMaking));
    }

    public void unloadedWeapons (String username){
        Player player= userToPlayer(username);
        List<String> unloadedWeapons= new ArrayList<>();
        for (Weapon w: player.showWeapons())
            unloadedWeapons.add(w.getName());
        notify(new ReloadableWeaponsEvent(username, unloadedWeapons));
    }

    public void usernameDeletion(String username){
        notify(new UsernameDeletionEvent("*", username));
    }

    public void closeMatchMaking(List<String> usernames){
        int colourCounter=0;
        List<String> configurations= new ArrayList<>();

        for (String userCounter: usernames){
            userLookup.add(new Pair<>(FigureColour.values()[colourCounter], userCounter));
            players.add(new Player(new Figure(FigureColour.values()[colourCounter]), this));

            colourCounter++;
        }

        //calls JSON Helper and generates all possible configurations
        //configurations to be assigned

        notify(new MatchConfigurationEvent("*", configurations));
    }

    public void configMatch (String chosenConfig, boolean isFinalFrenzy, int skulls){
        HashMap<String, FigureColour> userToColour= new HashMap<>();
        Map<String, RoomColour> weaponSpots= new HashMap<>();
        Map<Point, String> lootCards= new HashMap<>();
        Grabbable drawnGrabbable;

        for (Player player: players)
            userToColour.put(userLookup.getSecond(player.getFigure().getColour()), player.getFigure().getColour());

        //TODO: initialize gameMap

        for (Tile t: gameMap.getSpawnTiles()){
            for (int i=0; i<3; i++){
                drawnGrabbable= (Weapon)weaponDeck.draw();
                t.add(drawnGrabbable);
                weaponSpots.put(drawnGrabbable.getName(), t.colour);
            }
        }

        for (Tile t: gameMap.getLootTiles()){
            drawnGrabbable= (LootCard)lootDeck.draw();
            t.add(drawnGrabbable);
            lootCards.put(t.position, drawnGrabbable.getName());
        }

        notify(new MatchMakingEndEvent("*", chosenConfig, userToColour, weaponSpots, lootCards, skulls));
    }

    public void startMatch(){
        PowerUp firstCard= (PowerUp) powerUpDeck.draw();
        PowerUp secondCard= (PowerUp) powerUpDeck.draw();
        notify(new StartFirstTurnEvent(colourToUser(players.get(0).getFigure().getColour()),
                firstCard.getName(),
                secondCard.getName(),
                true
                ));
    }

    public void startTurn (String playing){
        notify(new StartTurnEvent(playing));
    }

    public void endTurn (String username){
        Player player= userToPlayer(username);
    }

    public void pausePlayer (String username){
        Player player= userToPlayer(username);
        player.pause();
    }

    public void pausedPlayer (Player pausedPlayer){
        notify(new PausedPlayerEvent("*", colourToUser(pausedPlayer.getFigure().getColour())));
    }

    public void unpausePlayer (String username){
        Player player= userToPlayer(username);
        player.unpause();
    }

    public void unpausedPlayer (Player unpausedPlayer){
        notify(new UnpausedPlayerEvent("*", colourToUser(unpausedPlayer.getFigure().getColour())));
    }

    public Tile getTile (Point position){
        return gameMap.getTile(position);
    }

    public List<Integer> getFrenzyPointsToAssign() {
        return frenzyPointsToAssign;
    }

    public boolean isFinalFrenzy() {
        return finalFrenzy;
    }

    public Deck getLootDeck() { return lootDeck; }

    public Deck getPowerUpDeck() {
        return powerUpDeck;
    }

    public Deck getWeaponDeck() {
        return weaponDeck;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public KillshotTrack getKillshotTrack() {
        return killshotTrack;
    }

    public List<Player> getPlayers() { return players; }

    public BiSet<FigureColour, String> getUserLookup() {
        return userLookup;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void setUserLookup(BiSet<FigureColour, String> userLookup) {
        this.userLookup = userLookup;
    }

    public Player colourToPlayer (FigureColour figureColour){
        for (Player playerCounter: players){
            if (figureColour.equals(playerCounter.getFigure().getColour())){
                return playerCounter;
            }
        }
        return null;
    }

    public String colourToUser (FigureColour figureColour){
        return userLookup.getSecond(figureColour);
    }

    public Player userToPlayer (String username){
        return colourToPlayer(userLookup.getFirst(username));
    }

    public String playerToUser (Player player){
        return colourToUser(player.getFigure().getColour());
    }

    public Weapon nameToWeapon (String weaponName){
        return CardHelper.getInstance().findWeaponByName(weaponName);
    }

    public PowerUp nameToPowerUp (String powerUpName){
        return CardHelper.getInstance().findPowerUpByName(powerUpName);
    }

    public LootCard nameToLootCard (String lootCardName){
        return CardHelper.getInstance().findLootCardByName(lootCardName);
    }

    public List<Tear> getHp (String username){
        return userToPlayer(username).getHp();
    }

    public void deathHandler (Player deadPlayer){
        updateKillshotTrack(deadPlayer.getHp().get(10).getColour(), deadPlayer.getHp().size()==12);
        notify(new MVDeathEvent("*",
                colourToUser(deadPlayer.getFigure().getColour()),
                colourToUser(deadPlayer.getHp().get(10).getColour()),
                (deadPlayer.getHp().size()==12),
                killshotTrack.getNumberOfSkulls()==killshotTrack.getKillshot().size())); //if number of skulls equals dimension of killshot track, match is over
        //calculate points of all players, move all lines beneath this in DeathController
        //in DeathController for (Player p: players) calculatePoints and then verify if FF
        if (killshotTrack.getKillshot().size()==killshotTrack.getNumberOfSkulls()){
            if (finalFrenzy){
                frenzyUpdatePlayerStatus(deadPlayer);
                notify(new FinalFrenzyStartingEvent("*"));
            }
        }
    }

     public void updateKillshotTrack(FigureColour killer, boolean overkill){
        killshotTrack.addKillshot(killer, overkill);
         if (killshotTrack.getKillshot().size()==killshotTrack.getNumberOfSkulls())
             notify(new FinalFrenzyStartingEvent("*"));
     }

     public TurnMemory getTurnMemory (){
        return new TurnMemory(turnMemory);
     }

     public List<Integer> getPointsToAssign (String username){
        Player player = userToPlayer(username);
        List<Integer> points= new ArrayList<>();
        //checks whether player is in FinalFrenzy
         if (player.getHealthState().isFinalFrenzy())
            points = frenzyPointsToAssign;
         else
             points = pointsToAssign;
        int index = points.indexOf(player.getPlayerValue().getMaxValue());
        return new ArrayList<>(pointsToAssign.subList(index, pointsToAssign.size()));
     }

    //exposed methods, used for MVEvents or VCEvents

    public void allowedMovements (String username, int radius){
        Player playing= userToPlayer(username);
        List<Point> allowedPositions= new ArrayList<>();
        if (!gameMap.getAllowedMovements(playing.getFigure().getTile(), radius).isEmpty())
            notify(new AllowedMovementsEvent(username, allowedPositions));
        else
            throw new NullPointerException("List of possible movements is empty");
    }

    public void allowedWeapons(String username){
        Player playing= userToPlayer(username);
        List<String> weapons= new ArrayList<>();
        for (Weapon weapon: playing.showWeapons()){
            weapons.add(weapon.getName());
        }
        notify(new AllowedWeaponsEvent(username, weapons));
    }
    public void teleportPlayer (String username, Point teleportPosition){
        Player playerToMove= userToPlayer(username);
        playerToMove.teleport(teleportPosition);
    }

    public void reloadWeapon (String username, String weaponName){
        Player playerReloading= userToPlayer(username);
        if (weaponName.equals(playerReloading.getFirstWeapon().getName())){
            playerReloading.reload(playerReloading.getFirstWeapon());
        }
        else if (weaponName.equals(playerReloading.getSecondWeapon().getName())){
            playerReloading.reload(playerReloading.getSecondWeapon());
        }
        else if (weaponName.equals(playerReloading.getThirdWeapon().getName())){
            playerReloading.reload(playerReloading.getThirdWeapon());
        }
    }

    public void run (String username, Point destination){
        Player playerRunning= userToPlayer(username);
        playerRunning.run(destination);
    }

    public void playerMovement (Player playerRunning, Point finalPosition){
        notify(new MVMoveEvent("*", colourToUser(playerRunning.getFigure().getColour()), finalPosition));
    }

    public void grabbableCards (String username){
        Player player= userToPlayer(username);
        List<String> grabbableNames= new ArrayList<>();
        List<Grabbable> grabbables= player.getFigure().getTile().grab();
        for (Grabbable g: grabbables)
            grabbableNames.add(g.getName());
        notify(new GrabbablesEvent(username, grabbableNames));

    }

    public void grab (String username, String grabbed){
        Player playerGrabbing= userToPlayer(username);
        Grabbable grabbedCard= null;
        for (Weapon weapon: CardHelper.getInstance().getAllWeapons()){
            if (weapon.getName().equalsIgnoreCase(grabbed)){
                grabbedCard= nameToWeapon(grabbed);
                break;
            }
        }
        if (grabbedCard==null){
            for (LootCard lootCard: CardHelper.getInstance().getAllLootCards()){
                if (lootCard.getName().equalsIgnoreCase(grabbed)){
                    grabbedCard= nameToLootCard(grabbed);
                }
            }
        }
        if (grabbedCard==null) throw new NullPointerException("This card is not grabbable");
        playerGrabbing.grabStuff(grabbedCard);
    }

    public void sendAvailableWeapons (String username){
        Player shooting= userToPlayer(username);
        List<String> availableWeapons= new ArrayList<>();
        for (Weapon weapon: shooting.showWeapons()){
            availableWeapons.add(weapon.getName());
        }
        notify(new AvailableWeaponsEvent(username, availableWeapons));
    }

    public void sendPossibleEffects (String username, String weaponName){
        Weapon weapon= nameToWeapon(weaponName);

        /*notify(new PossibleEffectsEvent(username,
                weaponName,
                weapon.getCardColour().getColour().toString(),
                ));*/
    }

    public void sendPossibleTargets (String username, List<Player> players, List<Tile> tiles, boolean isArea) {
        List<String> usernames = new ArrayList<>();
        List<Point> points = new ArrayList<>();

        for (Player p : players) {
            usernames.add(playerToUser(p));
        }
    }


    public GraphNode<GraphWeaponEffect> getWeaponEffects (String weapon){
        return nameToWeapon(weapon).getDefinition();
    }

    public void spawn (String username, AmmoColour spawnColour, String powerUpName){
        Player spawning= userToPlayer(username);
        PowerUp drawnPowerUp= nameToPowerUp(powerUpName);
        for (Tile tile: gameMap.getSpawnTiles()){
            if (tile.getColour().toString().equals(spawnColour.toString())){
                spawning.run(tile.position);
            }
        }
        if (drawnPowerUp!=null){
            spawning.drawPowerUp(drawnPowerUp.name);
        }
        notify(new MVMoveEvent("*", username, spawning.getFigure().getPosition()));
        notify(new StartTurnEvent(username));
    }

    public void usePowerUp (String username, String powerUpName){
        userToPlayer(username).usePowerUp(powerUpName);
    }

    public void chosePowerUpToDiscard (Player player, List<PowerUp> powerUps){
        String username= colourToUser(player.getFigure().getColour());
        List<String> powerUpsToDiscard= new ArrayList<>();
        for (PowerUp powerUp: powerUps){
            powerUpsToDiscard.add(powerUp.getName());
        }
        notify(new PowerUpToLeaveEvent(username, powerUpsToDiscard));
    }

    public void discardPowerUp (String username, String powerUpName){
        Player playing= userToPlayer(username);
        PowerUp powerUpToDiscard= nameToPowerUp(powerUpName);
        playing.discardPowerUp(powerUpToDiscard.name);
    }

    public void discardedPowerUp (Player player, String drawnPowerUp, String discardedPowerUp){
        notify(new DiscardedPowerUpEvent("*", colourToUser(player.getFigure().getColour()), drawnPowerUp, discardedPowerUp));
    }

    public void attackOnPlayer (Player attacked, Player attacker){
        notify(new UpdateHpEvent("*", colourToUser(attacked.getFigure().getColour()), colourToUser(attacker.getFigure().getColour())));
    }

    public void markOnPlayer (Player marked, Player marker){
        notify(new UpdateMarkEvent("*", colourToUser(marked.getFigure().getColour()), colourToUser(marker.getFigure().getColour())));
    }

    public void updatePoints (String username, int points){
        userToPlayer(username).setPoints(userToPlayer(username).getPoints()+points);
        notify(new UpdatePointsEvent(username, userToPlayer(username).getPoints()));
    }


    // all players without any damage change their boards to final frenzy boards
    // final frenzy players get a different set of moves based on their position in the current
    public void frenzyUpdatePlayerStatus (Player deadPlayer){
        List<Player> beforeFirst = new ArrayList<>();
        List<Player> afterFirst = new ArrayList<>();
        boolean isBeforeFirst = false;

        for (Player p: players){
            if (p.equals(deadPlayer))
                isBeforeFirst = true;
            if (!isBeforeFirst && p.getHp().size()==0)
                afterFirst.add(p);
            else if (isBeforeFirst && p.getHp().size()==0)
                beforeFirst.add(p);
        }

        for (Player p: beforeFirst)
            p.updatePlayerDamage(new FinalFrenzyBeforeFirst());

        for (Player p: afterFirst)
            p.updatePlayerDamage(new FinalFrenzyStandard());
    }
}
