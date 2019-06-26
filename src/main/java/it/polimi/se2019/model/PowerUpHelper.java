package it.polimi.se2019.model;

import it.polimi.se2019.utility.Log;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class PowerUpHelper extends JsonHelper{
    public PowerUpHelper() {
        this.create();
    }
    public PowerUpHelper(PowerUpHelper power){
        this.helped=power.helped;
    }

    @Override
    public void create() {
        Set<Jsonable> allPowerUps=new HashSet<>();
        try {
            for (String n : Paths.get("files/powerUps").toFile().list()) {
                helped.add((new PowerUp(Paths.get("files/powerUps/".concat(n)).toString())));
            }
        }catch (NullPointerException e){
            Log.severe("PowerUp not found in the powerUp directory");
        }
    }

    public Set<PowerUp> getPowerUps(){
        Set<PowerUp> powerUps= new HashSet<>();
        for (Jsonable j: this.getAll()){
            powerUps.add((PowerUp)j);
        }
        return powerUps;
    }
}
