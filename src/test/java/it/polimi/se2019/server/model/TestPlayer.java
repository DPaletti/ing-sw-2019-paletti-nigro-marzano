package it.polimi.se2019.server.model;
import it.polimi.se2019.commons.mv_events.PausedPlayerEvent;
import it.polimi.se2019.commons.mv_events.PowerUpToLeaveEvent;
import it.polimi.se2019.commons.mv_events.UnpausedPlayerEvent;
import it.polimi.se2019.commons.utility.BiSet;
import it.polimi.se2019.commons.utility.Pair;
import it.polimi.se2019.commons.utility.Point;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class TestPlayer {
    private Game model= new Game();
    private Player lulic=new Player(new Figure(FigureColour.GREEN),model);
    private Player marusic=new Player(new Figure(FigureColour.GREY),model);
    private Weapon whisper=(Weapon)model.getWeaponHelper().findByName("Whisper");
    private String partial= whisper.getWeaponEffects().iterator().next().getEffects().iterator().next().getName();
    private BiSet<FigureColour, String> lookup= new BiSet<>();
    private List<Player> playerList=new ArrayList<>();
    private TestModelHelper testModelHelper=new TestModelHelper();
    @Before
    public void setup(){
        String partial= whisper.getWeaponEffects().iterator().next().getEffects().iterator().next().getName();
        List<Targetable> players=new ArrayList<>();
        players.add(marusic);
        model.hit(partial,players,lulic);
        playerList.add(marusic);
        playerList.add(lulic);
        lookup.add(new Pair<>(FigureColour.GREEN, "green"));
        lookup.add(new Pair<>(FigureColour.GREY, "grey"));
        model.setUserLookup(lookup);
        model.setPlayers(playerList);
        model.register(testModelHelper);
        model.setGameMap(new GameMap("Small"));
    }

    @Test
    public void testHit(){
        List<Player> players1=new ArrayList<>();
        players1.add(marusic);
        assertEquals(players1, model.getTurnMemory().getHitTargets().get(partial));
        assertEquals(partial,model.getTurnMemory().getLastEffectUsed());
    }

    @Test
    public void testGetByEffects(){
        List<Targetable> players=new ArrayList<>();
        players.add(marusic);
        List<String> part=new ArrayList<>();
        part.add(partial);
        assertEquals(players,lulic.getByEffect(part,model.getTurnMemory()));
    }

    @Test
    public void testGetAll(){
        List<Targetable> targetables= new ArrayList<>(playerList);
        assertEquals(targetables,lulic.getAll());
    }

   @Test
   public void testPausing(){
        lulic.pause();
        assertTrue(lulic.isPaused());
        assertEquals(model.colourToUser(lulic.getFigure().getColour()),((PausedPlayerEvent)testModelHelper.getCurrent()).getPausedPlayer());
        lulic.unpause();
        assertFalse(lulic.isPaused());
        assertEquals(model.colourToUser(lulic.getFigure().getColour()),((UnpausedPlayerEvent)testModelHelper.getCurrent()).getUnpausedPlayer());
   }

   @Test(expected = UnsupportedOperationException.class)
    public void testUnPauseException(){
        lulic.unpause();
   }

    @Test(expected = UnsupportedOperationException.class)
    public void testPauseException(){
        lulic.pause();
        lulic.pause();
    }

   @Test
    public void testRunAndGrab(){
        Point redSpawnPoint= new Point(0,1);
        model.getGameMap().getTile(redSpawnPoint).add(whisper);
        lulic.getFigure().spawn(new Point(1,1));
        lulic.run(redSpawnPoint,-1);
        assertEquals(lulic.getPosition(),redSpawnPoint);
        lulic.grabStuff("Whisper");
        assertEquals(lulic.getWeapons().get(0).getName(),"Whisper");
   }

   @Test
    public void testReload(){
        List<Ammo> ammos= new ArrayList<>();
        ammos.add(new Ammo(AmmoColour.BLUE));
        ammos.add(new Ammo(AmmoColour.BLUE));
        ammos.add(new Ammo(AmmoColour.BLUE));
        lulic.useAmmos(ammos);
        lulic.reload(whisper);
        lulic.addAmmo(new Ammo(AmmoColour.BLUE));
        lulic.addAmmo(new Ammo(AmmoColour.BLUE));
        lulic.addAmmo(new Ammo(AmmoColour.BLUE));
        whisper.setLoaded(false);
        lulic.reload(whisper);
        assertTrue(whisper.getLoaded());
   }

  @Test
  public void testSellPowerUp(){
    Point randomLoot= new Point(1,1);
    LootCard loot= (LootCard)model.getLootCardHelper().findByName("PBR");
    model.getGameMap().getTile(randomLoot).add(loot);
    lulic.getFigure().spawn(randomLoot);
    List<Ammo> ammos=new ArrayList<>();
    ammos.add(new Ammo(AmmoColour.BLUE));
    lulic.useAmmos(ammos);
    assertEquals(11,lulic.getAmmo().size());
    lulic.grabStuff("PBR");
    assertEquals(12,lulic.getAmmo().size());
    assertEquals(1,lulic.getPowerUps().size());
    ammos.add(new Ammo(AmmoColour.RED));
    ammos.add(new Ammo(AmmoColour.YELLOW));
    lulic.useAmmos(ammos);
    lulic.sellPowerUp(lulic.getPowerUps().get(0).getName());
    assertEquals(10,lulic.getAmmo().size());
  }

  @Test(expected = UnsupportedOperationException.class)
    public void testPowerUpNotOwnedSell(){
        lulic.sellPowerUp("TeleportRed");
  }
    @Test(expected = UnsupportedOperationException.class)
    public void testPowerUpNotOwnedDiscard(){
        lulic.discardPowerUp("TeleportRed");
    }

    @Test
    public void testDiscardPowerUp(){
        lulic.drawPowerUp("TargetingScopeRed");
        lulic.drawPowerUp("NewtonRed");
        lulic.drawPowerUp("NewtonBlue");
        assertEquals(3,lulic.getPowerUps().size());
        lulic.discardPowerUp("NewtonRed");
        assertEquals(2,lulic.getPowerUps().size());
    }

    @Test
    public void testPowerUpsToPayEmptyReturn(){
        List<Ammo> ammos= new ArrayList<>();
        ammos.add(new Ammo(AmmoColour.YELLOW));
        assertEquals(Collections.emptyList(),lulic.powerUpsToPay(ammos));
    }

    @Test
    public void testGetLoadedWeapons(){
        whisper.setLoaded(true);
        lulic.addWeapon(whisper);
        List<Weapon> loaded= new ArrayList<>();
        loaded.add(whisper);
        assertEquals(loaded,lulic.getLoadedWeapons());

    }

}
