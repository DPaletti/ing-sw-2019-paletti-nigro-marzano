package it.polimi.se2019.network;

import it.polimi.se2019.utility.JsonHandler;
import it.polimi.se2019.utility.Log;
import it.polimi.se2019.view.DisconnectionEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;

public class ConnectionRMI implements Connection{
    private SynchronousQueue<String> in = new SynchronousQueue<>();
    private SynchronousQueue<String> out = new SynchronousQueue<>();

    private String ip;

    Timer timer = new Timer();
    TimerTask timeout = new TimerTask() {
            @Override
            public void run() {
                try {
                    DisconnectionEvent disconnectionEvent = new DisconnectionEvent(InetAddress.getByName(ip));
                    in.put(JsonHandler.serialize(disconnectionEvent, disconnectionEvent.getClass().toString().replace("class ", "")));
                }catch(InterruptedException e){
                    Log.severe(e.getMessage());
                    Thread.currentThread().interrupt();
                }catch (UnknownHostException e){
                    Log.severe(e.getMessage());
                }

            }
        };


    public ConnectionRMI(String ip){
        this.ip = ip;
        timer.schedule(timeout, 400);
        //TODO make this configurable and compliant with various game phases
    }

    @Override
    public String retrieve() {
        try {
            return in.take();
        }catch (InterruptedException e){
            Log.severe(e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void submit(String data) {
        try {
            out.put(data);
        }catch (InterruptedException e){
            Log.severe(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public InetAddress getRemoteEnd() {
        try {
            return InetAddress.getByName(ip);
        }catch (UnknownHostException e){
            Log.severe("RMI: cannot convert this string to an ip");
            return null;
        }
    }

    public void ping(){
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    DisconnectionEvent disconnectionEvent = new DisconnectionEvent(InetAddress.getByName(ip));
                    in.put(JsonHandler.serialize(disconnectionEvent, disconnectionEvent.getClass().toString().replace("class ", "")));
                }catch(InterruptedException e){
                    Log.severe(e.getMessage());
                    Thread.currentThread().interrupt();
                }catch (UnknownHostException e){
                    Log.severe(e.getMessage());
                }

            }
        }, 200);
        //TODO make it configurable

    }

    public Queue<String> getOut() {
        return out;
    }

    public Queue<String> getIn() {
        return in;
    }

}