package it.polimi.se2019.server.controller;

import it.polimi.se2019.client.view.VCEvent;
import it.polimi.se2019.commons.mv_events.MVChooseAmmoToPayEvent;
import it.polimi.se2019.commons.mv_events.PossibleEffectsEvent;
import it.polimi.se2019.commons.utility.JsonHandler;
import it.polimi.se2019.commons.utility.Log;
import it.polimi.se2019.commons.vc_events.ChosenEffectPowerUpEvent;
import it.polimi.se2019.commons.vc_events.PowerUpUsageEvent;
import it.polimi.se2019.commons.vc_events.VCChooseAmmoToPayEvent;
import it.polimi.se2019.commons.vc_events.VCPartialEffectEvent;
import it.polimi.se2019.server.model.*;
import it.polimi.se2019.server.network.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PowerUpController extends CardController {

    public PowerUpController(Game model, Server server, int roomNumber) {
        super(model, server, roomNumber);
    }

    public PowerUpController(Game model) {
        super(model);
    }

    @Override
    public void update(VCEvent message) {
        if(disabled)
            return;
        try {
            message.handle(this);
        } catch (UnsupportedOperationException e) {
            //ignore events that this controller does not support
            Log.fine("PowerUpController ignored " + JsonHandler.serialize(message));
        }
    }

    @Override
    public void dispatch(PowerUpUsageEvent message) {
        current = model.nameToPowerUp(message.getUsedPowerUp());
        currentPlayer = model.userToPlayer(message.getSource());
        currentPlayer.discardPowerUp(current.getName());
        layersVisited = layersVisited + 1;
        List<GraphWeaponEffect> list = new ArrayList<>();
        Ammo price=current.getWeaponEffects().iterator().next().getPrice().get(0);
        if(price.getColour().equals(AmmoColour.WHITE)){
            List<String> ammos=new ArrayList<>();
            for (Ammo a:currentPlayer.getAmmo()){
                ammos.add(a.getColour().name());
            }
            model.send(new MVChooseAmmoToPayEvent(message.getSource(),ammos));
        }
        PossibleEffectsEvent event = new PossibleEffectsEvent(model.playerToUser(currentPlayer), current.getName(), false);
        for (GraphWeaponEffect w: list)
            event.addEffect(w.getName(), w.getEffectType());
        model.send(event);
    }

    @Override
    public void dispatch(VCChooseAmmoToPayEvent message) {
        Ammo ammoToPay = null;
        for (AmmoColour a : AmmoColour.values()){
            if (a.name().equalsIgnoreCase(message.getChosenAmmo()))
                ammoToPay = new Ammo(a);
        }
        if (current != null && ammoToPay != null)
            model.userToPlayer(message.getSource()).useAmmos(new ArrayList<>(Arrays.asList(ammoToPay)));
    }

    @Override
    public void dispatch(ChosenEffectPowerUpEvent message) {
        for (GraphNode<GraphWeaponEffect> w: model.nameToPowerUp(message.getPowerUp()).getDefinition()) {
            if(w.getKey().getName().equals(message.getEffectName())) {
                weaponEffect = w.getKey();
                break;
            }
        }
        if(weaponEffect == null)
            throw new NullPointerException("Could not find " + message.getEffectName() + " in " + message.getPowerUp());

        handleEffect(false);
    }

    @Override
    public void dispatch(VCPartialEffectEvent message) {
        if (partialGraphLayer == -1 || message.isWeapon() || message.isSkip())
            return;
        if (current == null)
            return;
        if (message.getTargetPlayer() != null) {
            model.apply(model.playerToUser(currentPlayer),
                    new ArrayList<>(Arrays.asList(model.userToPlayer(message.getTargetPlayer()))),
                    currentLayer.get(partialGraphLayer).getKey());
        }
        else if (message.getTargetTile() != null) {
            List<Player> targets = new ArrayList<>();
            List<String> users = new ArrayList<>();
            for (Targetable t : model.getTile(message.getTargetTile()).getPlayers()) {
                targets.add((Player) t);
                users.add(model.playerToUser((Player)t));
            }
            model.apply(model.playerToUser(currentPlayer), targets, currentLayer.get(partialGraphLayer).getKey());
        }

        layersVisitedPartial++;
        currentLayer = weaponEffect.getEffectGraph().getListLayer(layersVisitedPartial);
        if (currentLayer.isEmpty()) {
            layersVisitedPartial = 0;
            nextWeaponEffect(false);
        }
        else {
            partialGraphLayer = 0;
            handlePartial(currentLayer.get(partialGraphLayer).getKey(),false);
        }
    }
}