package it.polimi.se2019.model;

import it.polimi.se2019.model.mv_events.*;
import it.polimi.se2019.utility.Action;
import it.polimi.se2019.utility.Point;

import java.util.*;

public class Player implements Targetable{
    private Figure figure;
    private boolean isPaused= false;
    private List<Tear> hp = new ArrayList<>();
    private PlayerDamage healthState = new Healthy();
    private PlayerValue playerValue = new NoDeaths();
    private Set<Tear> marks = new HashSet<>();
    private List<Weapon> weapons = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private Integer points = 0;
    private Set<Ammo> ammo = new HashSet<>();
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
        for (String s: effects){
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

    public PlayerDamage getHealthState() {
        return healthState;
    }

    public List<PowerUp> getPowerUps() {
        return new ArrayList<>(powerUps);
    }

    public List<Tear> getHp() {
        return new ArrayList<>(hp);
    }

    public Set<Tear> getMarks() {
        return new HashSet<>(marks);
    }

    public Set<Ammo> getAmmo() {
        return new HashSet<>(ammo);
    }

    public PlayerValue getPlayerValue() {
        return playerValue;
    }

    public void emptyHp (){
        this.hp.clear();
   }

    public void run (Point destination, int distance) {
        figure.run(destination, distance);
        game.send(new MVMoveEvent("*", game.colourToUser(figure.getColour()), destination));
    }

    public void shootPeople (Player target, int hits){
        for (int i = 0; i < hits; i++)
            figure.damage(target.figure);
    }

    public void markPeople (Player target, int marks){
        for (int i = 0; i< marks; i++)
            figure.mark(target.figure);
    }

    public void grabStuff(String grabbed){
        figure.grab(grabbed);
    }

    public void reload(Weapon weapon){
        if (pay(new HashSet<>(weapon.price)))
            figure.reload(weapon);
        else
            game.send(new NotEnoughAmmoEvent(game.colourToUser(figure.getColour())));
    }

    public void sellPowerUp (String powerUp){
        if (powerUpIsNotOwned(powerUp))
            throw new UnsupportedOperationException("Could not sell " + powerUp + "as player doesn't own it");
        ammo.add(new Ammo(game.nameToPowerUp(powerUp).getCardColour().getColour()));
        deletePowerUp(powerUp);
    }

    private void deletePowerUp (String powerUp){
        if (powerUpIsNotOwned(powerUp))
            throw new UnsupportedOperationException("Could not delete" + powerUp + "as player doesn't own it");
        powerUps.remove(game.nameToPowerUp(powerUp));
        game.send(new DiscardedPowerUpEvent("*", game.playerToUser(this), "none", powerUp));
    }

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

    void updatePlayerDamage (){
        if (hp.size()>=10) {
            updatePointsToAssign();
            game.deathHandler(this);
            return;
        }
        if(healthState.getMaximumHits()==hp.size())
            healthState= healthState.findNextHealthState();
    }

    void updatePlayerDamage (PlayerDamage playerDamage){
        healthState= playerDamage;
    }   //used for Final Frenzy state

    private void updatePointsToAssign (){
        playerValue= playerValue.getNextPlayerValue();
    }

    public void drawPowerUp (String drawnPowerUp){
        if (powerUps.size()==4)
            throw new UnsupportedOperationException("Discard a powerup before drawing one");
        powerUps.add(game.nameToPowerUp(drawnPowerUp));
        if (powerUps.size()==4) {
            game.send(new PowerUpToLeaveEvent(game.playerToUser(this), Card.cardStringify(Card.cardToCard(powerUps))));
        }
    }

    public void discardPowerUp (String discardedPowerUp){
        if (powerUpIsNotOwned(discardedPowerUp))
            throw new UnsupportedOperationException("Could not discard" + discardedPowerUp + "as player doesn't own it");
        game.send(new DiscardedPowerUpEvent("*", game.playerToUser(this), powerUps.get(3).name, discardedPowerUp));
        powerUps.remove(game.nameToPowerUp(discardedPowerUp));
    }

    public void useAmmos (Set<Ammo> usedAmmo){
       for (Ammo a : usedAmmo)
           useAmmo(a);
    }

    private void useAmmo  (Ammo usedAmmo){
        ammo.remove(usedAmmo);
    }

    public void addAmmo (Ammo ammo){
        this.ammo.add(ammo);
    }

    public void drawPowerUp (){
        drawPowerUp(game.getPowerUpDeck().draw().getName());
    }

    public boolean pay (Set<Ammo> ammoToPay){
        Set<Ammo> ammosOwned = new HashSet<>(ammoToPay);
        for  (Ammo a : ammoToPay){
            if (ammosOwned.contains(a))
                ammosOwned.remove(a);
            else
                return false;
        }
        useAmmos(ammoToPay);
        return true;
    }

    public void addWeapon (Weapon weapon){
        if (weapons.size() == 4)
            throw new UnsupportedOperationException("Discard a weapon before drawing one");
        weapons.add(weapon);
        if (weapons.size() == 4) {
            // game.send(/*new LeaveOneGrabbableEvent ()*/);
        }
    }

    public void apply (Player target, PartialWeaponEffect partialWeaponEffect){
        figure.shoot(partialWeaponEffect, target.figure);
    }
}