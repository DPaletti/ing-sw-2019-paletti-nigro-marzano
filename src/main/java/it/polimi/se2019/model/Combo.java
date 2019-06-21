package it.polimi.se2019.model;

import it.polimi.se2019.utility.Action;
import it.polimi.se2019.utility.JsonHandler;
import it.polimi.se2019.utility.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Combo implements Jsonable{
    private String name;
    //RUNAROUND, GRABSTUFF, SHOOTPEOPLE, FRENZYMOVERELOADSHOOT, FRENZYMOVEFOURSQUARES, FRENZYMOVEGRAB, FRENZYMOVETWICEGRABSHOOT, FRENZYMOVETHRICESHOOT
    private List<PartialCombo> partialCombos;

    public String getName() {
        return name;
    }

    public Combo(String path){
    try {
        Combo combo = (Combo) JsonHandler.deserialize(new String(Files.readAllBytes(Paths.get(path))));
        partialCombos=combo.getPartialCombos();
        name=combo.getName();
    }catch (IOException c){
        Log.severe("Combo not found in given directory");
    }catch (NullPointerException e){
        Log.severe("Combo not created: ");
    }catch (ClassNotFoundException e){
        Log.severe("Error in json file, type");
    }
    }

    public Combo(Combo combo){
        this.name=combo.getName();
        this.partialCombos=combo.getPartialCombos();
    }

    public List<PartialCombo> getPartialCombos() {
        return partialCombos;
    }

    @Override
    public String toString() {
        return "Combo{" +
                "name='" + name + '\'' +
                ", partialCombos=" + partialCombos +
                '}';
    }

    @Override
    public Jsonable copy() {
        return new Combo(this);
    }
}
