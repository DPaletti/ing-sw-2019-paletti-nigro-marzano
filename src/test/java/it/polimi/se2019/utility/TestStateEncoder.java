package it.polimi.se2019.utility;

import it.polimi.se2019.model.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;

import static junit.framework.TestCase.assertEquals;

//import sun.jvm.hotspot.oops.Mark;

@RunWith(MockitoJUnitRunner.class)
public class TestStateEncoder {
    Player player=new Player(new Figure(FigureColour.MAGENTA),new Game());
    String user;
    String file;

    @Before
    public void setup(){
        player.setFirstWeapon(CardHelper.getInstance().findWeaponByName("Cyberblade"));
        player.setSecondWeapon(CardHelper.getInstance().findWeaponByName("Shotgun"));
        player.setThirdWeapon(CardHelper.getInstance().findWeaponByName("Furnace"));
        player.setHp(new ArrayList<>());
        player.setMarks(new HashSet<>());
        player.setFirstPowerUp(CardHelper.getInstance().findPowerUpByName("TeleportRed"));
        player.setSecondPowerUp(CardHelper.getInstance().findPowerUpByName("TeleportBlue"));
        player.setThirdPowerUp(CardHelper.getInstance().findPowerUpByName("TeleportYellow"));
        player.setPoints(3);
        player.setUsableAmmo(new HashSet<>());
        user= "Lulic71";
        file= StateEncoder.generateEncodedGame();
    }

    @Test
    public void testGenerateEncodedGame(){
        assertEquals("<Players><&>"+System.lineSeparator()+"<Board><£>"+System.lineSeparator()+"<Last><@>"+System.lineSeparator(),
                StateEncoder.generateEncodedGame());
    }

    @Ignore
    @Test
    public void testGetEncodedUser(){
        assertEquals("Lulic71:M;;;8;Cyberblade;Shotgun;Furnace;TelR;TelB;TelY;3;R0,B0,Y0;0,0"+System.lineSeparator(),StateEncoder.getEncodedUser(player,user));
    }

    @Test
    public void testAddPlayer(){
        file=StateEncoder.addPlayer(player,user,file);
        System.out.println(file);
    }

    @Test
    public void testAddLastUser(){
        file=StateEncoder.addLastUser(user,file);
        System.out.println(file);
    }


}
