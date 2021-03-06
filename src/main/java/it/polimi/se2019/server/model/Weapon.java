package it.polimi.se2019.server.model;

import it.polimi.se2019.commons.mv_events.GrabbedWeaponEvent;
import it.polimi.se2019.commons.mv_events.MVSellPowerUpEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the weapons created dynamically through the WeaponHandler {@link it.polimi.se2019.server.model.WeaponHelper}.
 * A weapon can either be loaded or not and, being both a card and an object that can be grabbed, it implements both Grabbable and Drawable.
 */

public class Weapon extends Card implements Grabbable, Drawable, Jsonable{
    private boolean loaded = true;

    public boolean getLoaded() {
        return loaded;
    }

    public Weapon (String path){
            super(path);
    }

    public Weapon(Weapon weapon){
        super(weapon);
        this.loaded=weapon.loaded;
    }

    public void setLoaded(Boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Checks whether the number of weapons in hand is less than 3.
     * If that is true, the cost of the grab is payed and the weapon is added to the weapons owned.
     * @param player the player grabbing.
     * @param grabbed the weapon they wish to grab.
     * @param game
     */
    @Override
    public void grab(Player player, String grabbed, Game game) {
        int index =-1;
        for (Grabbable w : player.getFigure().getTile().getGrabbables()){
            if (w.getName().equalsIgnoreCase(grabbed)) {
               index = player.getFigure().getTile().getGrabbables().indexOf(w);
               break;
            }
        }
        if (index == -1)
            throw new UnsupportedOperationException(grabbed + "\t is not in the current weapon spot and cannot be grabbed");
        if (player.getWeapons().size() < 3){
            if(player.missingAmmos(((Weapon)player.getFigure().getTile().grabbables.get(index)).price).isEmpty()) {  //price could be and was paid
                player.addWeapon((Weapon) player.getFigure().getTile().grabbables.get(index));
                game.send(new GrabbedWeaponEvent("*", grabbed, game.playerToUser(player)));
                player.getFigure().getTile().removeGrabbed(grabbed);
                game.addEmptySpawnTile(player.getPosition());
            }
            else{
                if (!player.powerUpsToPay(((Weapon)player.getFigure().getTile().grabbables.get(index)).price).isEmpty()) {
                    Map<String, Integer> colourToMissing = new HashMap<>();
                    for (Ammo a : (player.missingAmmos(((Weapon)player.getFigure().getTile().grabbables.get(index)).price))) {
                        if (colourToMissing.containsKey(a.getColour().name()))
                            colourToMissing.put(a.getColour().name(), colourToMissing.get(a.getColour().name()) + 1);
                        else
                            colourToMissing.put(a.getColour().name(), 1);
                    }
                    game.send(new MVSellPowerUpEvent(game.playerToUser(player),
                            player.powerUpsToPay(((Weapon)player.getFigure().getTile().grabbables.get(index)).price),
                            colourToMissing));
                }
            }
        }
    }


}
