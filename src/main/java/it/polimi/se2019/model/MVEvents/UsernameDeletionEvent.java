package it.polimi.se2019.model.MVEvents;


import it.polimi.se2019.view.MVEvent;

public class UsernameDeletionEvent extends MVEvent {

    public UsernameDeletionEvent(String username){
        super(username);
    }

}
