package it.polimi.se2019.model;

import it.polimi.se2019.model.mv_events.MVSelectionEvent;
import it.polimi.se2019.utility.Action;
import it.polimi.se2019.utility.Point;

import java.util.*;

//TODO: change protected to private and override getters

public class Tile implements Targetable{
    protected GameMap gameMap;
    protected RoomColour colour;
    protected Map<Direction, Boolean> doors;
    protected Set<Figure> figures=new HashSet<>();
    protected Point position;
    protected List<Grabbable> grabbables=new ArrayList<>();

    protected Tile() { }

    public List<Grabbable> grab (){throw new UnsupportedOperationException("Can't grab from generic tile");}

    public List<Grabbable> getGrabbables() {
        return grabbables;
    }

    public void setGrabbables(List<Grabbable> grabbables) {
        this.grabbables = grabbables;
    }

    public void setColour(RoomColour colour) {
        this.colour = colour;
    }

    public void setDoors(Map<Direction, Boolean> doors) {
        this.doors = doors;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public RoomColour getColour() {
        return colour;
    }

    public Map<Direction, Boolean> getDoors() {
        return doors;
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    public void add(Grabbable grabbable){throw new UnsupportedOperationException("Can't add Tile");}

    public void addAll (List<Grabbable> grabbablesToAdd){throw new UnsupportedOperationException("Can't add Tiles");}


    @Override
    public void hit(String partialWeaponEffect, List<Targetable> hit, TurnMemory turnMemory) {
        List<Tile> list = new ArrayList<>();
        for(int i = 0; i < hit.size(); i++){
            list.add((Tile) hit.get(0));
        }
        turnMemory.putTiles(partialWeaponEffect, list);
        turnMemory.setLastEffectUsed(partialWeaponEffect);
    }

    @Override
    public List<Targetable> getByEffect(List<String> effects, TurnMemory turnMemory) {
        List<Tile> hit= new ArrayList<>();
        for (String s: effects){
            hit.addAll(turnMemory.getHitTiles().get(s));
        }
        return new ArrayList<>(hit);
    }

    @Override
    public List<Targetable> getAll() {
        return new ArrayList<>(gameMap.getTiles());
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
        List<Tile> tiles = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        for(Targetable t: targets)
            tiles.add((Tile) t);
        for(Tile t: tiles)
            points.add(t.getPosition());
        event.addActionOnTiles(actions, points);
    }

    private List<Tile> toTileList(List<Targetable> list){
        List<Tile> tiles = new ArrayList<>();
        for (Targetable t: list) {
            tiles.add((Tile) t);
        }
        return tiles;
    }
}
