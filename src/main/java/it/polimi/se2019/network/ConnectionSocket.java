package it.polimi.se2019.network;


import it.polimi.se2019.model.mv_events.SyncEvent;
import it.polimi.se2019.utility.JsonHandler;
import it.polimi.se2019.utility.Log;
import it.polimi.se2019.view.MVEvent;
import it.polimi.se2019.view.VCEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConnectionSocket implements Connection{
    private Scanner in;
    private PrintWriter out;

    private String token;

    private boolean disconnected = false;
    private List<MVEvent> eventBuffer = new ArrayList<>();

    public ConnectionSocket(String token, Socket socket){
        try{
            this.token = token;
            this.in = new Scanner(socket.getInputStream());
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }catch (IOException e){
            Log.severe("Cannot establish connection with" + socket.getInetAddress());
        }
    }

    @Override
    public void reconnect() {
        disconnected = false;
        submit(new SyncEvent(getToken(), eventBuffer));
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void submit(MVEvent mvEvent) {

        if(!disconnected)
            out.println(JsonHandler.serialize(mvEvent));
        else
            eventBuffer.add(mvEvent);
    }

    @Override
    public VCEvent retrieve(){
        try {
            return (VCEvent) JsonHandler.deserialize(in.nextLine());
        }catch (ClassNotFoundException e){
            Log.severe("Retrieving interrupted");
            throw new NullPointerException("Cannot deserialize");
        }
    }

    @Override
    public void disconnect() {
        disconnected = true;
    }

    @Override
    public String toString() {
        return ("{Connection type: " + "Socket " +
                "Connection token: " + token + "}");
    }
}
