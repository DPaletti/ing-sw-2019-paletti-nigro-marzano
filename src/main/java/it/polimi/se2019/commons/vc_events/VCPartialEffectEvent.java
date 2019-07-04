package it.polimi.se2019.commons.vc_events;

import it.polimi.se2019.commons.utility.Point;
import it.polimi.se2019.commons.utility.VCEventDispatcher;
import it.polimi.se2019.client.view.VCEvent;

public class VCPartialEffectEvent extends VCEvent {
    private boolean skip;
    private String targetPlayer;
    private Point targetTile;
    private boolean isWeapon;


    public VCPartialEffectEvent(String source) {
        super(source);
        this.skip = true;
    }

    public VCPartialEffectEvent(String source, String targetPlayer, boolean isWeapon) {
        super(source);
        this.skip = false;
        this.targetPlayer = targetPlayer;
        this.isWeapon = isWeapon;
    }

    public VCPartialEffectEvent(String source, Point targetTile, boolean isWeapon) {
        super(source);
        this.skip = false;
        this.targetTile = targetTile;
        this.isWeapon = isWeapon;
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean isWeapon() {
        return isWeapon;
    }

    @Override
    public void handle(VCEventDispatcher dispatcher) {
        dispatcher.dispatch(this);
    }

    public String getTargetPlayer() {
        return targetPlayer;
    }

    public Point getTargetTile() {
        return targetTile;
    }
}
