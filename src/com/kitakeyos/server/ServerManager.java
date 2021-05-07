package com.kitakeyos.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.ArrayList;
/**
 *
 * @author ASD
 */
public class ServerManager {

    private   static boolean debug;
    protected static String host;
    protected static short  post;
    protected static String mysql_host;
    protected static String mysql_user;
    protected static String mysql_pass;
    protected static String mysql_database;

    protected static int numClients;
    protected static ArrayList<ClientEntry> clients;
    protected static ServerSocket server;
    protected static boolean start;
    protected static int id;
    public static byte itemVersion;
    public static byte mapVersion;
    public static byte dataVersion;
    public static byte skillVersion;

    private static void loadConfigFile() {
        byte[] ab = NinjaSchool.getFile("ninja.conf");
        if(ab == null) {
            System.out.println("Config file not found!");
            System.exit(0);
        }
        String data = new String(ab);
        HashMap<String, String> configMap = new HashMap<>();
        StringBuilder sbd = new StringBuilder();
        boolean bo = false;
        for(int i = 0; i <= data.length(); i++) {
            char es;
            if((i == data.length()) || ((es = data.charAt(i)) == '\n')) {
                bo = false;
                String sbf = sbd.toString().trim();
                if(sbf != null && !sbf.equals("") && sbf.charAt(0) != '#') {
                    int j = sbf.indexOf(':');
                    if(j > 0) {
                        String key = sbf.substring(0, j).trim();
                        String value = sbf.substring(j + 1).trim();
                        configMap.put(key, value);
                        System.out.println("config: "+key+"-"+value);
                    }
                }
                sbd.setLength(0);
                continue;
            }
            if(es == '#')
                bo = true;
            if(!bo)
                sbd.append(es);
        }
        if(configMap.containsKey("debug")) {
            debug = Boolean.parseBoolean(configMap.get("debug"));
        } else {
            debug = false;
        }
        if(configMap.containsKey("host")) {
            host = configMap.get("host");
        } else {
            host = "localhost";
        }
        if(configMap.containsKey("post")) {
            post = Short.parseShort(configMap.get("post"));
        } else {
            post = 8122;
        }
        if(configMap.containsKey("mysql-host")) {
            mysql_host = configMap.get("mysql-host");
        } else {
            mysql_host = "localhost";
        }
        if(configMap.containsKey("mysql-user")) {
            mysql_user = configMap.get("mysql-user");
        } else {
            mysql_user = "root";
        }
        if(configMap.containsKey("mysql-password")) {
            mysql_pass = configMap.get("mysql-password");
        } else {
            mysql_pass = "";
        }
        if(configMap.containsKey("mysql-database")) {
            mysql_database = configMap.get("mysql-database");
        } else {
            mysql_database = "ninjaschoold";
        }
        if(configMap.containsKey("dataVersion")) {
            dataVersion = Byte.parseByte(configMap.get("dataVersion"));;
        } else {
            dataVersion = 0;
        }
        if(configMap.containsKey("mapVersion")) {
            mapVersion = Byte.parseByte(configMap.get("mapVersion"));;
        } else {
            mapVersion = 0;
        }
        if(configMap.containsKey("itemVersion")) {
            itemVersion = Byte.parseByte(configMap.get("itemVersion"));;
        } else {
            itemVersion = 0;
        }
        if(configMap.containsKey("skillVersion")) {
            skillVersion = Byte.parseByte(configMap.get("skillVersion"));
        } else {
            skillVersion = 0;
        }
    }

    protected static void init() {
        start = false;
        loadConfigFile();
        SQLManager.create(mysql_host, mysql_database, mysql_user, mysql_pass);
    }

    protected static void start() {
        System.out.println("Start socket post="+post);
        try {
            clients = new ArrayList<>();
            server = new ServerSocket(post);
            id = 0;
            numClients = 0;
            start = true;
            log("Start server Success!");
            while(start) {
                try {
                    Socket client = server.accept();
                    ClientEntry cl = new ClientEntry(client, ++id);
                    clients.add(cl);
                    numClients++;
                    log("Accept socket " + cl + " done!");
                } catch(IOException e) {
                    System.out.println(e.toString());
                }
            }         
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    protected static void stop() {
        if(start) {
            close();
            start = false;
            System.gc();
        }
    }

    protected static void close() {
        try {
            server.close();
            server = null;
            while(clients.size() > 0) {
                ClientEntry c = clients.get(0);
                c.close();
                numClients--;
            }
            clients = null;
            SQLManager.close();
            System.gc();
            System.out.println("End socket");
        } catch(IOException e) {
            System.out.println(e.toString());
        }
    }

    public static void log(String s) {
        if(debug)
            System.out.println(s);
    }
    
    protected static void disconnect(ClientEntry cl) {
        synchronized(clients) {
            clients.remove(cl);
            numClients--;
            System.out.println("Disconnect client: " + cl);
        }
    }
}
