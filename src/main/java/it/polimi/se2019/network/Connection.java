package it.polimi.se2019.network;


import java.rmi.RemoteException;

public interface Connection {
    void submit(String data);
    String retrieve();
    String getToken();
    void setToken(String token) throws RemoteException;
}
