package it.polimi.se2019.model;

import it.polimi.se2019.utility.JsonHandler;
import it.polimi.se2019.utility.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public abstract class Card {
    protected Ammo cardColour;
    protected List<Ammo> price;
    protected Set<WeaponEffect> weaponEffects;
    protected String name;
    protected int maxHeight;
    protected String constraint;
    private GraphNode<GraphWeaponEffect> staticDefinition=new GraphNode<>(null,0);

    public Card(String path) {
        try {
            Card card = (Card) JsonHandler.deserialize(new String(Files.readAllBytes(Paths.get(path))));
            this.cardColour = card.cardColour;
            this.maxHeight = card.maxHeight;
            this.name = card.name;
            this.price = card.price;
            this.weaponEffects = card.weaponEffects;
            this.constraint=card.constraint;
            defineCard();
        }catch (IOException c){
            Log.severe("Card not found in given directory");
        }catch (NullPointerException e){
            Log.severe("Card not created: ");
        }catch (ClassNotFoundException e){
            Log.severe("Error in json file, type");
        }
    }

    public Card(Card card){
        this.cardColour=card.cardColour;
        this.price=card.price;
        this.weaponEffects=card.weaponEffects;
        this.name=card.name;
        this.maxHeight=card.maxHeight;
        this.constraint=card.constraint;
        this.staticDefinition=card.staticDefinition;

    }

    private void defineCard(){
        Set<GraphWeaponEffect> graphWeaponEffects= new HashSet<>();
        for (WeaponEffect weaponEffect: weaponEffects){
            GraphWeaponEffect graphWeaponEffect= new GraphWeaponEffect(weaponEffect, this);
            graphWeaponEffects.add(graphWeaponEffect);
        }
        generateGraph(graphWeaponEffects,staticDefinition,maxHeight,maxHeight);

    }

    public GraphNode<GraphWeaponEffect> getDefinition(){
        return staticDefinition;
    }

    public String getName() {
        return name;
    }

    public Set<WeaponEffect> getWeaponEffects() {
        return weaponEffects;
    }

    public Ammo getCardColour() {
        return cardColour;
    }

    public List<Ammo> getPrice() {
        return price;
    }

    public String getConstraint() {
        return constraint;
    }

    public List<Ammo> getReloadPrice(){
        List<Ammo> reloadPrice = new ArrayList<>();
        reloadPrice.add(cardColour);
        reloadPrice.addAll(price);
        return reloadPrice;
    }

    public static List<String> cardStringify(List<Card> cards){
        List<String> names = new ArrayList<>();
        for (Card c : cards)
            names.add(c.getName());
        return names;
    }

    public static List<Card> cardToCard(List<? extends Card> cards){
        List<Card> toReturn = new ArrayList<>();
        Iterator iterator= cards.iterator();
        while (iterator.hasNext()){
            toReturn.add((Card)iterator.next());
        }
        return toReturn;
    }

    public  <T extends Effect> void generateGraph(Set<T> effectSet, GraphNode<T> root, int maxHeight, int constantMaxHeight){
        if (maxHeight==0)
            return;
        int actualMax=0;
        for (T effect : effectSet) {
            if (effect.priority > actualMax)
                actualMax = effect.priority;
        }
        for (T effect : effectSet) {
            Set<T> childrenSet= new HashSet<>(effectSet);
            if (effect.priority == actualMax) {
                GraphNode<T> node = new GraphNode<>(effect,constantMaxHeight-maxHeight+1);
                root.addChild(node);
                for (String string : effect.invalidEffects)
                    childrenSet.remove(getEffect(string, effectSet));
                childrenSet.remove(effect);
                int newHeight=maxHeight-1;
                generateGraph(childrenSet, node,newHeight,constantMaxHeight);
            }
        }
    }

    private <T extends Effect> T getEffect(String name,Set<T> effectSet){
        for(T effect: effectSet){
            if (effect.getName().equals(name))
                return effect;
        }
        throw new NullPointerException("Can't find effect");
    }
}
