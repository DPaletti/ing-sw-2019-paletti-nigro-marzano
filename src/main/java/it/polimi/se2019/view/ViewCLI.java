package it.polimi.se2019.view;


import it.polimi.se2019.model.mv_events.MatchMakingEndEvent;
import it.polimi.se2019.network.Client;

import java.util.List;

public class ViewCLI extends View {
    private Dispatcher dispatcher = new Dispatcher();

    public ViewCLI(Client client){
        super(client);
    }

    @Override
    public void matchMaking(List<String> usernames) {

    }

    @Override
    public void addPlayer(String username) {

    }

    private class Dispatcher extends CommonDispatcher {

        @Override
        public void update(MatchMakingEndEvent message){
            //TODO stuff
        }


    }

    @Override
    public void update(MVEvent message) {
        message.handle(dispatcher);
    }
}
