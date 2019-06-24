package it.polimi.se2019.model;
import it.polimi.se2019.utility.BiSet;
import it.polimi.se2019.utility.Pair;
import it.polimi.se2019.utility.Point;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.*;

public class TestFigure {
    private Figure blueFigure= new Figure(FigureColour.BLUE);
    private Figure magentaFigure= new Figure(FigureColour.MAGENTA);
    private Game model= new Game();
    private Point redSpawnPoint=new Point(0,1);
    private BiSet<FigureColour, String> lookup= new BiSet<>();

    @Before
    public void setup(){
        model.setGameMap(new GameMap("Small"));
        blueFigure.setPlayer(new Player(blueFigure,model));
        magentaFigure.setPlayer(new Player(magentaFigure,model));
        blueFigure.spawn(redSpawnPoint);
        magentaFigure.spawn(redSpawnPoint);
        lookup.add(new Pair<>(FigureColour.MAGENTA, "magenta"));
        lookup.add(new Pair<>(FigureColour.BLUE, "blue"));
        model.setUserLookup(lookup);
        blueFigure.getTile().add((Weapon)model.getWeaponDeck().draw());
    }

    @Test
    public void testGetPosition(){
        assertEquals(redSpawnPoint,blueFigure.getPosition());
    }

   @Test
   public void testGetColour(){
        assertEquals(FigureColour.BLUE,blueFigure.getColour());
   }

   @Test
    public void testGetTile(){
        assertEquals(blueFigure.getPlayer().getGameMap().getTile(redSpawnPoint),blueFigure.getTile());
   }

   @Test
    public void testRun(){
        Point newPosition= new Point(0,2);
        blueFigure.run(newPosition,1);
        assertEquals(newPosition,blueFigure.getPosition());
   }

   @Test
    public void testDamage(){
        blueFigure.mark(magentaFigure);
        blueFigure.damage(magentaFigure);
        List<Tear> testList= new ArrayList<>();
        testList.add(new Tear(FigureColour.BLUE));
        testList.add(new Tear(FigureColour.BLUE));
        assertEquals(testList,magentaFigure.getPlayer().getHp());
   }

    @Test
    public void testMark(){
        blueFigure.mark(magentaFigure);
        blueFigure.mark(magentaFigure);
        assertEquals(new Tear(FigureColour.BLUE),magentaFigure.getPlayer().getMarks().iterator().next());
    }


    @Test
    public void testGrab(){
        String weaponName=blueFigure.getTile().getGrabbables().get(0).getName();
        System.out.print(weaponName);
        blueFigure.grab(weaponName);
        assertEquals(blueFigure.getPlayer().getWeapons().get(0).getName(),weaponName);
    }

    @Test
    public void testLoad(){
        String weaponName=blueFigure.getTile().getGrabbables().get(0).getName();
        blueFigure.grab(weaponName);
        blueFigure.unload(blueFigure.getPlayer().getWeapons().get(0));
        boolean loaded=blueFigure.getPlayer().getWeapons().get(0).getLoaded();
        assertFalse(loaded);
        blueFigure.reload(blueFigure.getPlayer().getWeapons().get(0));
        loaded=blueFigure.getPlayer().getWeapons().get(0).getLoaded();
        assertTrue(loaded);
    }




}