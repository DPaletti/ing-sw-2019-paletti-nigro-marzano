package it.polimi.se2019.server.model;

import it.polimi.se2019.commons.mv_events.AllowedMovementsEvent;
import it.polimi.se2019.commons.mv_events.MVDeathEvent;
import it.polimi.se2019.commons.mv_events.ReloadableWeaponsEvent;
import it.polimi.se2019.commons.mv_events.UsablePowerUpEvent;
import it.polimi.se2019.commons.utility.BiSet;
import it.polimi.se2019.commons.utility.Pair;
import it.polimi.se2019.commons.utility.Point;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestGame {
    private Game game= new Game();
    private Player leiva= new Player(new Figure(FigureColour.GREEN),game);
    private Player wallace= new Player(new Figure(FigureColour.MAGENTA),game);
    private Player ciro= new Player(new Figure(FigureColour.GREY),game);
    private WeaponHelper weaponHelper=game.getWeaponHelper();
    private ComboHelper comboHelper=game.getComboHelper();
    private Point redSpawnPoint= new Point(0,1);
    private BiSet<FigureColour, String> lookup= new BiSet<>();
    private List<Player> playerList=new ArrayList<>();
    private TestModelHelper testModelHelper=new TestModelHelper();
    private KillshotTrack killshotTrack= new KillshotTrack(2);

    @Before
    public void setup(){
        lookup.add(new Pair<>(FigureColour.GREEN, "leiva"));
        lookup.add(new Pair<>(FigureColour.MAGENTA, "wallace"));
        lookup.add(new Pair<>(FigureColour.GREY,"ciro"));
        playerList.add(leiva);
        playerList.add(wallace);
        playerList.add(ciro);
        game.setUserLookup(lookup);
        game.setPlayers(playerList);
        game.register(testModelHelper);
        game.setKillshotTrack(killshotTrack);
        game.setGameMap(new GameMap("Small"));
        game.getGameMap().getTile(redSpawnPoint).add((Weapon)weaponHelper.findByName("Heatseeker"));
        leiva.getFigure().spawn(redSpawnPoint);
        leiva.grabStuff("Heatseeker");
        wallace.getFigure().spawn(redSpawnPoint);


    }
    @Test
    public void testApply(){
        PartialWeaponEffect partial= leiva.getWeapons().get(0).getWeaponEffects().iterator().next().getEffects().iterator().next();
        List<Player> targets=new ArrayList<>();
        targets.add(wallace);
        game.apply(game.playerToUser(leiva),targets,partial);
        assertEquals(3,wallace.getHp().size());
    }

   @Test
    public void testPlayerDeath(){
       PartialWeaponEffect partial= leiva.getWeapons().get(0).getWeaponEffects().iterator().next().getEffects().iterator().next();
       List<Player> targets=new ArrayList<>();
       targets.add(wallace);
       game.apply(game.playerToUser(leiva),targets,partial);
       assertEquals(6,(int)wallace.getHealthState().getMaximumHits());
       game.apply(game.playerToUser(leiva),targets,partial);
       game.apply(game.playerToUser(leiva),targets,partial);
       game.apply(game.playerToUser(leiva),targets,partial);
       assertEquals(1,(long)game.getKillshotTrack().getKillshot().size());
       assertEquals(8,wallace.getPlayerValue().getMaxValue());
       MVDeathEvent message= (MVDeathEvent)testModelHelper.getCurrent();
       assertEquals(game.playerToUser(leiva),message.getKiller());
       assertEquals(game.playerToUser(wallace),message.getDead());
       assertTrue(message.isOverkill());
       assertFalse(message.isMatchOver());
   }

   @Test
    public void testFinalFrenzy(){
       PartialWeaponEffect partial= leiva.getWeapons().get(0).getWeaponEffects().iterator().next().getEffects().iterator().next();
       List<Player> targets=new ArrayList<>();
       targets.add(wallace);
       for(int i=0;i<5;i++)
           game.apply(game.playerToUser(leiva),targets,partial);
       for(int i=0;i<5;i++)
           game.apply(game.playerToUser(ciro),targets,partial);
        List<List<PartialCombo>> afterFirst= new ArrayList<>();
        afterFirst.add(((Combo)comboHelper.findByName("FrenzyMoveMoveReloadShoot")).getPartialCombos());
        afterFirst.add(((Combo)comboHelper.findByName("FrenzyMoveThreeGrab")).getPartialCombos());
        assertEquals(afterFirst,leiva.getHealthState().getMoves());
        MVDeathEvent message= (MVDeathEvent) testModelHelper.getCurrent();
        assertEquals(game.playerToUser(wallace),message.getDead());
        assertEquals(game.playerToUser(leiva),message.getKiller());
        assertFalse(message.isMatchOver());
        assertTrue(message.isOverkill());
        assertEquals("*",message.getDestination());

   }

   @Test
    public void testGetMapConfig(){
        List<String> configs=new ArrayList<>();
        configs.add("Large");
        configs.add("Small");
        configs.add("MediumRight");
        configs.add("MediumLeft");
        assertTrue(configs.containsAll(game.getMapConfigs()));
   }

   @Test
    public void testUnloadedWeapons(){
        leiva.getWeapons().get(0).setLoaded(false);
        game.unloadedWeapons(game.playerToUser(leiva));
        ReloadableWeaponsEvent message= (ReloadableWeaponsEvent)testModelHelper.getCurrent();
        assertTrue(message.getPriceMap().keySet().contains("Heatseeker"));
   }

   @Test
    public void testAllowedMovements(){
        game.allowedMovements("",game.playerToUser(leiva),2);
       AllowedMovementsEvent message= (AllowedMovementsEvent)testModelHelper.getCurrent();
       Point p1=new Point(0,1);
       Point p2=(new Point(0,2));
       Point p3=(new Point(1,2));
       assertTrue(message.getAllowedPositions().contains(p1));
       assertTrue(message.getAllowedPositions().contains(p2));
       assertTrue(message.getAllowedPositions().contains(p3));
   }

   @Test
    public void testUsablePowerUps(){
       Point randomLoot= new Point(1,1);
       LootCardHelper lootCardHelper= game.getLootCardHelper();
       LootCard loot= (LootCard)lootCardHelper.findByName("PBR");
       game.getGameMap().getTile(randomLoot).add(loot);
       leiva.getFigure().spawn(randomLoot);
       leiva.grabStuff("PBR");
       game.usablePowerUps(leiva.getPowerUps().get(0).getConstraint(),false,leiva);
       UsablePowerUpEvent message= (UsablePowerUpEvent)testModelHelper.getCurrent();
       assertEquals(leiva.getPowerUps().get(0).getName(),message.getUsablePowerUp());
   }

   @Test
    public void testPauseUser(){
        game.pausePlayer(game.playerToUser(leiva));
        assertTrue(leiva.isPaused());
        game.unpausePlayer(game.playerToUser(leiva));
        assertFalse(leiva.isPaused());
   }
}
