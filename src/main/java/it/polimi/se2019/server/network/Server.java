package it.polimi.se2019.server.network;

import it.polimi.se2019.client.view.MVEvent;
import it.polimi.se2019.client.view.VCEvent;
import it.polimi.se2019.commons.mv_events.MatchConfigurationEvent;
import it.polimi.se2019.commons.mv_events.SyncEvent;
import it.polimi.se2019.commons.network.CallbackInterface;
import it.polimi.se2019.commons.network.ServerInterface;
import it.polimi.se2019.commons.utility.Log;
import it.polimi.se2019.commons.utility.Pair;
import it.polimi.se2019.commons.utility.Point;
import it.polimi.se2019.server.controller.Controller;
import it.polimi.se2019.server.controller.MatchMakingController;
import it.polimi.se2019.server.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Server implements ServerInterface {
    private ServerSocket serverSocket;
    private List<VirtualView> rooms = new ArrayList<>();
    private List<Game> models = new ArrayList<>();
    private int roomNumber = -1;
    private List<String> usernames = new ArrayList<>();
    private List<String> activeUsernames = new ArrayList<>();
    private List<String> tokens = new ArrayList<>();
    private Properties properties = new Properties();

    private Semaphore semRMI = new Semaphore(1, true);

    private boolean isMatchMaking = true;
    private boolean isSetUp = false;

    private Connection suspendedConnection = null;

    public Server(){

        usernames.add("*");
        activeUsernames.add("*");

        try {
            fillProperties();
            openConnections();

            rooms.add(new VirtualView(0, this));
            newMatch();
        }catch (AlreadyBoundException e){
            Log.severe("Could not bind RMI interface");
        }catch (IOException e){
            Log.severe("IO error while starting it.polimi.se2019.client.network up");
        }
    }

    public Server(int roomNumber){
        this.roomNumber=roomNumber;
    }

    public int getDefaultSkulls(){
        return  Integer.parseInt(properties.getProperty("DEFAULT_SKULLS"));
    }

    private void startSetup(){
        isSetUp = true;
    }

    private void endSetup(){
        isSetUp = false;
    }

    public boolean isSetUp() {
        return isSetUp;
    }

    public int getTurnTimer(){
        return Integer.parseInt(properties.getProperty("TURN_TIMER"));
    }

    public int getInterTurnTimer(){
        return Integer.parseInt(properties.getProperty("INTERTURN_TIMER"));
    }

    private String getAddress(){
        return properties.getProperty("ADDRESS");
    }

    public List<String> getMapConfigs(int roomNumber){
        return models.get(roomNumber).getMapConfigs();
    }

    public List<String> getUsernames() {
        return new ArrayList<>(usernames);
    }

    public List<String> getActiveUsernames() {
        return activeUsernames;
    }

    public List<String> getTokens() {
        return new ArrayList<>(tokens);
    }

    private int getPort(){
        return Integer.parseInt(properties.getProperty("PORT"));
    }

    public int getMatchMakingTimer(){
        return Integer.parseInt(properties.getProperty("MATCH_MAKING_TIMER"));
    }

    public int getMatchSetupTimer(){
        return Integer.parseInt(properties.getProperty("MATCH_SETUP_TIMER"));
    }

    public void addUsername(String username){
        usernames.add(username);
        activeUsernames.add(username);
    }

    public void deleteUsername(String username){
        usernames.remove(username);
        activeUsernames.remove(username);
    }

    public void disconnectUsername(String username){
        activeUsernames.remove(username);
    }

    public boolean isReconnection(String username){
        for(VirtualView v: rooms){
            if(v.getBiTokenUsername().containsSecond(username))
                return true;
        }
        return false;
    }

    public void addController(Controller controller, int roomNumber){
        rooms.get(roomNumber).register(controller);
    }

    public void removeController(Controller controller, int roomNumber){
        rooms.get(roomNumber).deregister(controller);
    }

    /**
     * player kicking (e.g. for a matchmaking disconnection)
     * @param toKick player to be kicked
     * @param isReconnection boolean to signal reconnection
     */
    public void kickPlayer(String toKick, boolean isReconnection) {
        VirtualView room = getPlayerRoomOnId(toKick);
        if(!isReconnection) {
            //player disconnected in match making
            usernames.remove(toKick);
            activeUsernames.remove(toKick);
        }
        else {
            List<VirtualView> roomsModified = rooms;
            roomsModified.remove(room);
            boolean found = false;
            for (VirtualView v : roomsModified) {
                if (v.getBiTokenUsername().containsFirst(toKick)) {
                    room = v;
                    found = true;
                    break;
                }

                if (v.getBiTokenUsername().containsSecond(toKick)) {
                    room = v;
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.severe("Could not find: " + toKick + " in a room other than " + room.getRoomNumber() + " (the one in which reconnection is happening)");
                return;
            }
        }

        if(room!=null)
            room.removePlayer(toKick);
    }

    public synchronized MatchConfigurationEvent setupSync(String usernameReconnecting, int roomNumber) {
        Game model = models.get(roomNumber);
        return new MatchConfigurationEvent(usernameReconnecting, model.getMapConfigs(), model.getUsernames(), true);
    }

    /**
     * SyncEvent creation given all the model's information
     *
     * @param roomNumber number of the room
     * @param usernameReconnecting username upon reconnections
     * @param token token for reconnection
     * @return SyncEvent
     */
    public synchronized SyncEvent sync(int roomNumber, String usernameReconnecting, String token){
        Game model = models.get(roomNumber);
        boolean isFrenzy = model.isFinalFrenzy();
        String leftConfig = model.getGameMap().getConfig().getLeftHalf();
        String rightConfig = model.getGameMap().getConfig().getRightHalf();
        List<String> configs = model.getMapConfigs();
        List<String> roomUsernames = new ArrayList<>();
        List<String> pausedPlayer = new ArrayList<>();
        int skulls = model.getKillshotTrack().getNumberOfSkulls() - model.getKillshotTrack().getKillshot().size();
        Map<String, Integer> points = new HashMap<>();
        List<String> powerup = new ArrayList<>();
        Map<String, ArrayList<String>> finance = new HashMap<>();
        Map<String, String> colour = new HashMap<>();
        Map<String, ArrayList<String>> weapon = new HashMap<>();
        Map<String, ArrayList<String>> mark = new HashMap<>();
        Map<String, ArrayList<String>> hp = new HashMap<>();
        Map<Point, String> loot = new HashMap<>();
        Map<String, String> weaponSpots = new HashMap<>();
        Map<Point, ArrayList<String>> figurePosition = new HashMap<>();
        Map<Point, String> poinToColourSpawn = model.getGameMap().getMappedSpawnPoints();

        ArrayList<String> stringStore;

        for(Tile s: model.getGameMap().getLootTiles())
            loot.put(s.getPosition(), s.getGrabbables().get(0).getName());

        for(Tile s: model.getGameMap().getSpawnTiles()){
            for(Grabbable g: s.getGrabbables())
                weaponSpots.put(g.getName(), s.getColour().name());
        }

        for (Tile s: model.getGameMap().getTiles()){
            figurePosition.put(s.getPosition(), new ArrayList<>());
            for(Figure g: s.getFigures())
                figurePosition.get(s.getPosition()).add(g.getColour().name());
        }

        String currentUsername;
        for(Player p: model.getPlayers()){
            currentUsername = model.playerToUser(p);

            roomUsernames.add(currentUsername);

            if(p.isPaused())//paused
                pausedPlayer.add(currentUsername);

            if(currentUsername.equals(usernameReconnecting)){//powerups
                for(PowerUp po: p.getPowerUps())
                    powerup.add(po.getName());
            }

            stringStore = new ArrayList<>();
            for(Ammo a: p.getAmmo())
                stringStore.add(a.getColour().name());
            finance.put(currentUsername, stringStore);

            for(Pair<FigureColour, String> e :model.getUserLookup())
                colour.put(e.getSecond(), e.getFirst().name());

            stringStore = new ArrayList<>();
            for(Weapon w: p.getWeapons())
                stringStore.add(w.getName());
            weapon.put(currentUsername, stringStore);

            stringStore = new ArrayList<>();
            for(Tear t: p.getMarks())
                stringStore.add(t.getColour().name());
            mark.put(currentUsername, stringStore);

            stringStore = new ArrayList<>();
            for(Tear t: p.getHp())
                stringStore.add(t.getColour().name());
            hp.put(currentUsername, stringStore);


            points.put(currentUsername, p.getPoints());
        }
        return new SyncEvent(token, poinToColourSpawn, figurePosition, weaponSpots, loot, hp, mark, weapon, colour, finance, powerup, points, skulls, pausedPlayer, roomUsernames, configs,leftConfig, rightConfig, isFrenzy);
    }


    public VirtualView getPlayerRoomOnId(String id){
        for (VirtualView v : rooms) {
            if (v.getBiTokenUsername().containsFirst(id)) {
                return v;
            }

            if (v.getBiTokenUsername().containsSecond(id)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Opens connections for socket and RMI protocols
     * @throws IOException
     * @throws AlreadyBoundException
     */
    private void openConnections()throws IOException, AlreadyBoundException{
        System.setProperty("java.rmi.server.hostname", getAddress());

        serverSocket = new ServerSocket(getPort());

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.bind(properties.getProperty("SERVER_NAME"), UnicastRemoteObject.exportObject(this, 0));
    }


    /**
     * Creates new MVC for multiple paralle lobbys
     *
     */

    protected void newMVC() {

        Game model = new Game();
        models.add(model);
        new MatchMakingController(model, this, roomNumber);


        model.register(rooms.get(roomNumber));
        if(suspendedConnection != null) {
            Log.fine("Un-suspending connection");
            rooms.get(roomNumber).startListening(suspendedConnection);
            suspendedConnection = null;
        }
        Log.info("Server ready");
        try {
            acceptClients();
        }catch (IOException e){
            Log.severe("Server socket has been closed" + e.getMessage());
        }
    }

    /**
     * Socket connnection accepting
     * @throws IOException Socket listen exception
     */
    private void acceptClients() throws IOException{
        semRMI.release();
        while(true){
            Socket socket = serverSocket.accept();
            if(!isMatchMaking) {
                isMatchMaking = true;
                rooms.add(new VirtualView(roomNumber + 1, this));
                suspendedConnection = new ConnectionSocket(generateToken(), socket);
                return;
            }
            if(rooms.size() == roomNumber)
                rooms.add(new VirtualView(roomNumber, this));
            rooms.get(roomNumber).startListening(new ConnectionSocket(generateToken(), socket));
          }
    }

    /**
     * Creates a new Match by creating a new room and a new MVC
     */
    private void newMatch(){
        while(!Thread.currentThread().isInterrupted()) {
            semRMI.acquireUninterruptibly();
            roomNumber++;
            newMVC();
            Log.fine("Creating new game");
        }
    }

    public void endMatchMaking(){
        Log.fine("Ending matchMaking");
        isMatchMaking = false;
    }

    private void fillProperties(){
        try {
            properties.load(new FileInputStream(Paths.get("files/server.properties").toFile()));
        }catch (IOException e){
            Log.severe("Could not load properties");
        }
    }

    private String generateToken(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        String token = Base64.getEncoder().encodeToString(bytes);
        if(tokens.contains(token))
            generateToken();
        tokens.add(token);
        return token;
    }

    //------------------------RMI REMOTE SERVER INTERFACE IMPLEMENTATION------------------------//

    /**
     * Listening for RMI connections
     * @param client callback parameter
     */
    @Override
    public void startListening(CallbackInterface client) {
        semRMI.acquireUninterruptibly();
        Log.fine("Accepted new client");
        if(!isMatchMaking) {
            isMatchMaking = true;
            semRMI.release();
            rooms.add(new VirtualView(roomNumber + 1, this));
            suspendedConnection = new ConnectionRMI(generateToken(), client, roomNumber + 1, rooms.get(roomNumber + 1));
            newMatch();
            return;
        }
        rooms.get(roomNumber).startListening(new ConnectionRMI(generateToken(), client, roomNumber, rooms.get(roomNumber)));
        semRMI.release();
    }


    @Override
    public MVEvent pullEvent(String token, int playerRoomNumber) throws RemoteException {
        try {
            return ((ConnectionRMI) rooms.get(playerRoomNumber).getConnectionOnToken(token)).pull();
        } catch (NullPointerException e) {
            Log.severe("You detain an invalid token: " + token + "on room number: " + playerRoomNumber);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void pushEvent(String token, VCEvent vcEvent, int playerRoomNumber) throws RemoteException {
        try{
            ((ConnectionRMI) rooms.get(playerRoomNumber).getConnectionOnToken(token)).push(vcEvent);
        }catch (NullPointerException e){
            Log.severe("You detain an invalid token: " + token + "on room number: " + playerRoomNumber);
            e.printStackTrace();
        }
    }

    //------------------------RMI REMOTE SERVER INTERFACE IMPLEMENTATION------------------------//

    public static void main(String[] args){
        new Server();
    }

}
