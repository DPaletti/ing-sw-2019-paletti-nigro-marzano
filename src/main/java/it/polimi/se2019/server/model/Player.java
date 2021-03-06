package it.polimi.se2019.server.model;

import it.polimi.se2019.commons.mv_events.*;
import it.polimi.se2019.commons.utility.Point;

import java.util.*;

/**
 * This class implements the player of the game. Each player is defined by its figure {@link it.polimi.se2019.server.model.Figure}.
 * A player handles all the basic actions that are needed to play in a turn.
 * It implements Targetable {@link it.polimi.se2019.server.model.Targetable} as a player can be targeted by a weapon.
 */

public class Player implements Targetable{
    private Figure figure;
    private boolean isPaused= false;

    private List<Tear> hp = new ArrayList<>();
    private List<Tear> marks = new ArrayList<>();
    private Integer points = 0;

    private PlayerDamage healthState = new Healthy();
    private PlayerValue playerValue = new MatchStarted();

    private List<Weapon> weapons = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Ammo> ammo = new ArrayList<>();

    private PowerUp firstPowerUp;
    private PowerUp secondPowerUp;

    private Game game;

    public Player (Figure figure, Game game){
        this.figure= figure;
        this.game= game;
        for (AmmoColour ammoColour: AmmoColour.values()){
            for (int i=0; i<3; i++){
                ammo.add(new Ammo(ammoColour));
            }
        }
        this.figure.setPlayer(this);
    }

    // Targetable methods

    @Override
    public void hit (String partialWeaponEffect, List<Targetable> hit, TurnMemory turnMemory) {
        List<Player> list = new ArrayList<>();
        for (Targetable t: hit)
           list.add((Player) t);
        turnMemory.putPlayers(partialWeaponEffect, list);
        turnMemory.setLastEffectUsed(partialWeaponEffect);
    }

    @Override
     public List<Targetable> getByEffect (List<String> effects, TurnMemory turnMemory) {
        List<Targetable> hit= new ArrayList<>();
        for (String s: effects) {
            if (turnMemory.getHitTargets().get(s) != null)
                hit.addAll(turnMemory.getHitTargets().get(s));
        }
        return hit;
    }

    @Override
    public List<Targetable> getAll() {
        return new ArrayList<>(game.getPlayers());
    }

    @Override
    public Point getPosition() {
        return figure.getPosition();
    }

    @Override
    public Map<String, List<Targetable>> getHitTargets(TurnMemory turnMemory) {
        List<Targetable> list;
        Map<String, List<Targetable>> map = new HashMap<>();
        for (String s: turnMemory.getHitTargets().keySet()) {
            list = new ArrayList<>(turnMemory.getHitTargets().get(s));
            map.put(s, list);
        }
        return map;
    }

    @Override
    public void addToSelectionEvent(MVSelectionEvent event, List<Targetable> targets, List<Action> actions) {
        List<Player> players = new ArrayList<>(toPlayerList(targets));
        List<String> usernames = new ArrayList<>();
        for(Player p: players){
            usernames.add(game.playerToUser(p));
        }
        event.addActionOnPlayer(actions, usernames);
    }

    @Override
    public List<Targetable> getPlayers() {
        return new ArrayList<>(Arrays.asList(this));
    }

    private List<Player> toPlayerList (List<Targetable> list){
        List<Player> players = new ArrayList<>();
        for (Targetable t: list) {
            players.add((Player) t);
        }
        return players;
    }

    public GameMap getGameMap (){
        return game.getGameMap();
    }

    public void unpause (){
        if (!isPaused){
            throw new UnsupportedOperationException("This player is already unpaused");
        }
        isPaused = false;
        game.send(new UnpausedPlayerEvent("*", game.colourToUser(figure.getColour())));
    }

    public void pause(){
        if (isPaused){
            throw new UnsupportedOperationException("This player is already paused");
        }
        isPaused = true;
        game.send(new PausedPlayerEvent("*", game.colourToUser(figure.getColour())));
    }

    public void setPlayerValue(PlayerValue playerValue) {
        this.playerValue = playerValue;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public Figure getFigure() {
        return figure;
    }

    public Game getGame() {
        return game;
    }

    public PlayerDamage getHealthState() {
        return healthState;
    }

    public List<PowerUp> getPowerUps() {
        return new ArrayList<>(powerUps);
    }

    public List<Tear> getHp() {
        return new ArrayList<>(hp);
    }

    public List<Tear> getMarks() {
        return new ArrayList<>(marks);
    }

    public List<Ammo> getAmmo() {
        return new ArrayList<>(ammo);
    }

    public PlayerValue getPlayerValue() {
        return playerValue;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public PowerUp getFirstPowerUp() {
        return firstPowerUp;
    }

    public PowerUp getSecondPowerUp() {
        return secondPowerUp;
    }

    public void setFirstPowerUp(PowerUp firstPowerUp) {
        this.firstPowerUp = firstPowerUp;
    }

    public void setSecondPowerUp(PowerUp secondPowerUp) {
        this.secondPowerUp = secondPowerUp;
    }

    public void emptyHp (){
        this.hp.clear();
   }

    /**
     * moves a user to their preferred direction.
     * @param destination the preferred destination of the user.
     * @param distance the maximum distance they can run.
     */
    public void run (Point destination, int distance) {
        figure.run(destination, distance);
        game.send(new MVMoveEvent("*", game.colourToUser(figure.getColour()), destination));
    }

    /**
     * causes damage to a chosen targets.
     * @param target chosen player to target.
     * @param hits the number of hits to cause.
     */
    public void shootPeople (Player target, int hits){
        for (int i = 0; i < hits; i++)
            figure.damage(target.figure);
        target.updatePlayerDamage();
    }

    /**
     * causes marks to a chosen target.
     * @param target the chosen player to target.
     * @param marks the number of marks to cause.
     */
    public void markPeople (Player target, int marks){
        for (int i = 0; i< marks; i++)
            figure.mark(target.figure);
    }

    public void grabStuff(String grabbed){
        figure.grab(grabbed);
    }

    /**
     * reloads the weapon checking whether the user can afford the reload.
     * @param weapon the weapon the user wishes to reload.
     */

    public void reload(Weapon weapon){
        ArrayList<Ammo> reloadPrice= new ArrayList<>(weapon.price);
        reloadPrice.add(weapon.cardColour);
        if (missingAmmos(new ArrayList<>(reloadPrice)).isEmpty())
            figure.reload(weapon);
        else {
            if (!powerUpsToPay(reloadPrice).isEmpty()) {
                Map<String, Integer> colourToMissing = new HashMap<>();
                for (Ammo a : (missingAmmos(reloadPrice))) {
                    if (colourToMissing.containsKey(a.getColour().name()))
                        colourToMissing.put(a.getColour().name(), colourToMissing.get(a.getColour().name()) + 1);
                    else
                        colourToMissing.put(a.getColour().name(), 1);
                }
                game.send(new MVSellPowerUpEvent(game.playerToUser(this), powerUpsToPay(reloadPrice), colourToMissing));
            }
        }
    }

    /**
     * Sells a power up in order to use its ammo to pay for the price on an action.
     * @param powerUp chosen power up to sell.
     */
    public void sellPowerUp (String powerUp){
        if (powerUpIsNotOwned(powerUp))
            throw new UnsupportedOperationException("Could not sell " + powerUp + "as player doesn't own it");
        ammo.add(new Ammo(game.nameToPowerUp(powerUp).getCardColour().getColour()));
        discardPowerUp(powerUp);
    }

    /**
     * Checks whether the power up the user wants to use in actually owned.
     * @param powerUp name of the power up to use.
     * @return if the power up is owned, it returns false.
     */
    private boolean powerUpIsNotOwned(String powerUp){
        for (PowerUp p: powerUps){
            if (p.name.equalsIgnoreCase(powerUp))
                return false;
        }
        return true;
    }

    void addTear (FigureColour figureColour){
        hp.add(new Tear(figureColour));
        game.send(new UpdateHpEvent("*", game.colourToUser(figure.getColour()), game.colourToUser(figureColour)));
    }

    void addMark (FigureColour figureColour){
        marks.add(new Tear(figureColour));
        game.send(new UpdateMarkEvent("*", game.colourToUser(figure.getColour()), game.colourToUser(figureColour), true));
    }

    void removeMark (FigureColour figureColour){
        marks.remove(new Tear(figureColour));
        game.send(new UpdateMarkEvent("*", game.colourToUser(figure.getColour()), game.colourToUser(figureColour), false));
    }

    /**
     * Updates the moves a user is allowed to perform.
     */
    void updatePlayerDamage (){
        if (hp.size()>10) {
            updatePointsToAssign();
            game.deathHandler(this);
            return;
        }
        if(healthState.getMaximumHits()<=hp.size())
            healthState= healthState.findNextHealthState();
    }

    /**
     * Updates the moves a user is allowed to perform during final frenzy mode.
     * @param playerDamage
     */
    void updatePlayerDamage (PlayerDamage playerDamage){
        healthState= playerDamage;
    }   //used for Final Frenzy state


    private void updatePointsToAssign (){
        playerValue= playerValue.getNextPlayerValue();
    }

    public void drawPowerUp (String drawnPowerUp){
        if (powerUps.size()==4)
            throw new UnsupportedOperationException("Discard a powerup before drawing one");
        if (powerUps.size()==3) {
            game.send(new PowerUpToLeaveEvent(game.playerToUser(this), Card.cardStringify(Card.cardToCard(powerUps))));
        }else {
            powerUps.add(game.nameToPowerUp(drawnPowerUp));
        }
    }

    public void discardPowerUp (String discardedPowerUp){
        if (powerUpIsNotOwned(discardedPowerUp))
            throw new UnsupportedOperationException("Could not discard" + discardedPowerUp + "as player doesn't own it");
        game.getPowerUpDeck().discard(game.nameToPowerUp(discardedPowerUp));
        powerUps.remove(game.nameToPowerUp(discardedPowerUp));
    }

    public void useAmmos (List<Ammo> usedAmmo){
       for (Ammo a: usedAmmo)
           useAmmo(a);
       game.send(new FinanceUpdateEvent("*", game.playerToUser(this), ammoToString(ammo)));
    }

    private void useAmmo  (Ammo usedAmmo){
        ammo.remove(usedAmmo);
    }

    public void addAmmo (Ammo ammo){
        this.ammo.add(ammo);
        game.send(new FinanceUpdateEvent("*", game.playerToUser(this), ammoToString(this.ammo)));
    }

    public void drawPowerUp (){
        drawPowerUp(game.getPowerUpDeck().draw().getName());
    }

    /**
     * Calculates the ammos missing to pay the price of a certain action.
     * @param ammoToPay price of the action.
     * @return
     */
    public List<Ammo> missingAmmos (List<Ammo> ammoToPay){
        List<Ammo> ammosMissing = new ArrayList<>();
        List<Ammo> ammosOwned = new ArrayList<>(ammo);
        for  (Ammo a : ammoToPay){
            if (ammosOwned.contains(a))
                ammosOwned.remove(a);
            else
                ammosMissing.add(a);
        }
        if (ammosMissing.isEmpty())
            useAmmos(ammoToPay);
        return ammosMissing;
    }

    /**
     * adds a grabbed dweapon.
     * @param weapon
     */
    public void addWeapon (Weapon weapon){
        if (weapons.size() == 4)
            throw new UnsupportedOperationException("Discard a weapon before drawing one");
        if (weapons.size() == 3) {
            game.send(new WeaponToLeaveEvent(game.playerToUser(this),Card.cardStringify(Card.cardToCard(weapons))));
        }
        weapons.add(weapon);
    }

    public void apply (Player target, PartialWeaponEffect partialWeaponEffect){
        figure.shoot(partialWeaponEffect, target.figure, game);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return figure.equals(player.figure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(figure);
    }

    @Override
    public String toString() {
        return "Player{" +
                "figure=" + figure.toString() +
                '}';
    }

 public List<String> powerUpsToPay (List<Ammo> price){
        List<PowerUp> availablePowerUps = new ArrayList<>(powerUps);
        List<String> toPay = new ArrayList<>();
        boolean flag = false;
        for (Ammo a : price){
            for (PowerUp p : availablePowerUps){
                if (a.getColour().name().equalsIgnoreCase(p.getColour())) {
                    toPay.add(p.name);
                    powerUps.remove(p);
                    flag = true;
                }
                if (!flag)
                    return Collections.emptyList();
            }
        }
        return toPay;
 }


    /**
     * returns weapons that are already loaded and ready to be used.
     * @return
     */
 public List<Weapon> getLoadedWeapons(){
     List<Weapon> loadedWeapons=new ArrayList<>();
     for (Weapon w: weapons){
         if(w.getLoaded())
             loadedWeapons.add(w);
     }
     return loadedWeapons;
 }

 private List<String> ammoToString (List<Ammo> ammos){
        List<String> ammoNames = new ArrayList<>();
        for (Ammo a : ammos)
            ammoNames.add(a.getColour().name());
        return ammoNames;
 }

}