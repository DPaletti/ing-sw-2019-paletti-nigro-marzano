package it.polimi.se2019.model;

import it.polimi.se2019.utility.Factory;
import it.polimi.se2019.utility.Log;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.io.File;

public class WeaponDeck implements Deck {
    private List<Weapon> cards;

    @Override
    public void createCards(String directory) {
        try {
            for (File file : Paths.get(directory).toFile().listFiles()) {
                cards.add(Factory.createWeapon(file.getPath()));
            }
        }catch (NullPointerException e){
            Log.severe("Path given does not exist");
        }
    }

    @Override
    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Weapon> getCards() {
        return cards;
    }


}