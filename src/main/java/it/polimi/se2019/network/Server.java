package it.polimi.se2019.network;

import it.polimi.se2019.utility.Log;
import it.polimi.se2019.view.VirtualView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private VirtualView virtualView;
    private boolean socketOpen;

    private int port;
    //TODO make it configurable together with RMI registry port
    private static final int DEFAULT_PORT = 2080;

    private Server(int port){
        this.port = port;
        this.virtualView = new VirtualView();
        socketOpen = false;

    }

    private int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }

    private boolean isSocketOpen(){
        return socketOpen;
    }

    private void startServer() throws IOException, AlreadyBoundException {
        serverSocket = new ServerSocket(port);
        socketOpen = true;
        Registry registry = LocateRegistry.createRegistry(1099); //using default value
        registry.bind(RMIRemoteObjects.REMOTE_SERVER_NAME, virtualView);
        Log.info("Server ready");
        try {
            acceptClients();
        }catch (IOException e){
            Log.severe("Server socket has been closed" + e.getMessage());
        }
    }

    private void acceptClients() throws IOException{
        while(!serverSocket.isClosed()){
            Socket socket = serverSocket.accept();
            Log.fine("Accepted new client");
            virtualView.newEventLoop(new ConnectionSocket(socket));
        }
    }


    public static void main(String[] args){
        Scanner in = new Scanner(System.in);

        Log.input("Input socket port number (> 1024): ");
        Server server = new Server(in.nextInt());
        if(server.getPort() <= 1024){
            Log.info("This port does not work, default port will be used " + DEFAULT_PORT);
            server.setPort(DEFAULT_PORT);
        }

        try {
            server.startServer();
        }catch (IOException e){
            if(server.isSocketOpen())
                Log.severe("Could not get RMI registry");
            else
                Log.severe("Could not open server socket");
        }catch (AlreadyBoundException e){
            Log.severe("Could not bind server interface to RMI registry");
        }
    }

}
