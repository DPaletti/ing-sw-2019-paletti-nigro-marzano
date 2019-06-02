package it.polimi.se2019.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurnMemory {
    private Map<String, List<Player>>  hitTargets= new HashMap<>();
    private  Map<String, List<Tile>> hitTiles= new HashMap<>();
    private String lastEffectUsed= "none";

    public TurnMemory(Map<String, List<Player>> hitTargets, Map<String, List<Tile>> hitTiles) {

        this.hitTargets = hitTargets;
        this.hitTiles = hitTiles;
    }

    public TurnMemory (TurnMemory turnMemory){
        this.hitTiles= turnMemory.getHitTiles();
        this.hitTargets= turnMemory.getHitTargets();
    }

    public Map<String, List<Player>> getHitTargets() {
        return new HashMap<>(hitTargets);
    }

    public Map<String, List<Tile>> getHitTiles() {
        return new HashMap<>(hitTiles);
    }

    public String getLastEffectUsed() {
        return lastEffectUsed;
    }

    public void hitPlayers (String partialWeaponEffect, List<Player> hitPlayers){
        hitTargets.put(partialWeaponEffect, hitPlayers);
        lastEffectUsed=partialWeaponEffect;
    }

    public void hitTiles (String partialWeaponEffect, List<Tile> hitTiles){
        this.hitTiles.put(partialWeaponEffect, hitTiles);
        lastEffectUsed=partialWeaponEffect;
    }

    public void end (){
        hitTargets.clear();
        hitTiles.clear();
        lastEffectUsed= "none";
    }
}