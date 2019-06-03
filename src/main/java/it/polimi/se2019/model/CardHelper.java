package it.polimi.se2019.model;

import it.polimi.se2019.utility.Log;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

//Singleton class with all the static weapons and powerUps and methods to find them by name
public class CardHelper {
    private static CardHelper instance=null;
    private static Set<Weapon> allWeapons= new HashSet<>();
    private static Set<PowerUp> allPowerUp= new HashSet<>();
    private static Set<LootCard> allLootCards= new HashSet<>();
    private CardHelper(){
            for (String name : Paths.get("files/weapons").toFile().list()) {
                try {
                    allWeapons.add((new Weapon(Paths.get("files/weapons/".concat(name)).toString())));
                }catch (IOException e){
                    Log.severe("Could not find weapon");
                }catch (ClassNotFoundException e){
                    Log.severe("Could not find weapon class during deserialization");
                }
            }
            for (String name : Paths.get("files/powerUps").toFile().list()) {
                try {
                    allPowerUp.add(new PowerUp(Paths.get("files/powerUps/".concat(name)).toString()));
                }catch (IOException e){
                    Log.severe("Could not find powerup");
                }catch (ClassNotFoundException e){
                    Log.severe("Could not find powerup class during deserialization");
                }
            }
        for (AmmoColour topAmmoColour: AmmoColour.values()){
            for (AmmoColour bottomAmmoColour: AmmoColour.values()){
                if (!topAmmoColour.equals(bottomAmmoColour)) {
                    for (int i = 0; i < 3; i++) {
                        char[] name = {topAmmoColour.toString().charAt(0),
                                bottomAmmoColour.toString().charAt(0),
                                bottomAmmoColour.toString().charAt(0)};
                        String nameOfCard = new String(name);
                        LootCard lootCard = new LootCard(nameOfCard, topAmmoColour, bottomAmmoColour, bottomAmmoColour);
                        allLootCards.add(lootCard);
                    }
                }
                char[] name = {'P',
                                topAmmoColour.toString().charAt(0),
                                bottomAmmoColour.toString().charAt(0)};
                String nameOfCard= new String(name);
                LootCard lootCard= new LootCard(nameOfCard, topAmmoColour, bottomAmmoColour);
                allLootCards.add(lootCard);
            }
        }
    }

    public static CardHelper getInstance() {
        if (instance == null){
            instance= new CardHelper();
        }
        return instance;
    }

    public PowerUp findPowerUpByName(String name) {
        for (PowerUp powerUp : allPowerUp) {
            if (name.equalsIgnoreCase(powerUp.getName())) {
                return powerUp;
            }
        }
        throw new NullPointerException("PowerUp not found");
    }

    public Weapon findWeaponByName(String name){
        for (Weapon weapon : allWeapons){
            if (weapon.getName().equalsIgnoreCase(name)){
                return weapon;
            }
        }
        throw new NullPointerException("Weapon not found");
    }

    public LootCard findLootCardByName (String name){
        for (LootCard lootCard: allLootCards){
            if (lootCard.getName().equalsIgnoreCase(name)){
                return lootCard;
            }
        }
        throw new NullPointerException("LootCard not found");
    }

    public Set<PowerUp> getAllPowerUp() {
        return allPowerUp;
    }

    public Set<Weapon> getAllWeapons() {
        return allWeapons;
    }

    public Set<LootCard> getAllLootCards(){
        return allLootCards;
    }
}
