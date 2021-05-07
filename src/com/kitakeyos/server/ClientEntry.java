package com.kitakeyos.server;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.kitakeyos.network.Message;
import com.kitakeyos.network.ISession;
import com.kitakeyos.network.IMessageHandler;

/**
 *
 * @author ASD
 */
public class ClientEntry implements ISession {

    private class Sender implements Runnable {

        private final ArrayList<Message> sendingMessage;

        public Sender() {
            sendingMessage = new ArrayList<>();
        }

        public void AddMessage(Message message) {
            sendingMessage.add(message);
        }

        @Override
        public void run() {
            try {
                while(isConnected()) {
                    while(sendingMessage.size() > 0) {
                        Message m = sendingMessage.get(0);
                        ServerManager.log("Send mss "+m.getCommand()+" to "+ClientEntry.this.toString());
                        doSendMessage(m);
                        sendingMessage.remove(0);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MessageCollector implements Runnable {

        @Override
        public void run() {
            try {
                while(true) {
                    Message message = readMessage();
                    if(message != null) {
                        ServerManager.log(ClientEntry.this.toString()+" send mss "+message.getCommand());
                        messageHandler.onMessage(message);
                        message.cleanup();
                    } else
                        break;
                }
            } catch(Exception ex) {
            }
            System.out.println("Error read message from client " + ClientEntry.this);
            if(isConnected()) {
                if(messageHandler != null) {
                    messageHandler.onDisconnected();
                }
                close();
            }
        }

        private Message readMessage() throws Exception {
            // read message command
            byte cmd = dis.readByte();
            if(connected)
                cmd = readKey(cmd);
            // read size of data
            int size;
            if(connected) {
                byte b1 = dis.readByte();
                byte b2 = dis.readByte();
                size = (readKey(b1) & 0xff) << 8 | readKey(b2) & 0xff;
            } else
                size = dis.readUnsignedShort();
            byte data[] = new byte[size];
            int len = 0;
            int byteRead = 0;
            while(len != -1 && byteRead < size) {
                len = dis.read(data, byteRead, size - byteRead);
                if(len > 0)
                    byteRead += len;
            }
//            recvByteCount += (5 + byteRead);
            if(connected)
                for(int i = 0; i < data.length; i++) {
                    data[i] = readKey(data[i]);
                }
            Message msg = new Message(cmd, data);
            return msg;
        }
    }

    private static final byte[] key = "Leo".getBytes();
    public Socket sc;
    public DataInputStream dis;
    public DataOutputStream dos;
    public int id;
    public User user;
    private IMessageHandler messageHandler;
    protected boolean connected, login;
//    private int sendByteCount;
//    private int recvByteCount;
    private byte curR, curW;
    private final Sender sender;
    private Thread collectorThread;
    protected Thread sendThread;
    protected final Object obj = new Object();
    protected String plastfrom;
    protected String versionARM;
    protected byte clientType;
    protected byte zoomLevel;
    protected boolean isGPS;
    protected int width;
    protected int height;
    protected boolean isQwert;
    protected boolean isTouch;
    protected byte languageId;
    protected int provider;
    protected String agent;
    
    public ClientEntry(Socket sc, int id) throws IOException {
        this.sc = sc;
        this.id = id;
        this.dis = new DataInputStream(sc.getInputStream());
        this.dos = new DataOutputStream(sc.getOutputStream());
        setHandler(new MessageHandler(this));
        sendThread = new Thread(sender = new Sender());
        collectorThread = new Thread(new MessageCollector());
        collectorThread.start();
    }

    public void setClientType(Message mss) throws IOException {
        this.clientType = mss.reader().readByte();
        this.zoomLevel = mss.reader().readByte();
        this.isGPS = mss.reader().readBoolean();
        this.width = mss.reader().readInt();
        this.height = mss.reader().readInt();
        this.isQwert = mss.reader().readBoolean();
        this.isTouch = mss.reader().readBoolean();
        this.plastfrom = mss.reader().readUTF();
        mss.reader().readInt();
        mss.reader().readByte();
        this.languageId = mss.reader().readByte();
        this.provider = mss.reader().readInt();
        this.agent = mss.reader().readUTF();
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setHandler(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void sendMessage(Message message) {
        sender.AddMessage(message);
    }

    protected synchronized void doSendMessage(Message m) throws IOException {
        byte[] data = m.getData();
        try {
            if(connected) {
                byte b = (writeKey(m.getCommand()));
                dos.writeByte(b);
            } else
                dos.writeByte(m.getCommand());
            if(data != null) {
                int size = data.length;
                if(m.getCommand() == 90) {
                    dos.writeInt(size);
                    dos.write(data);
                } else {
                    if(connected) {
                        int byte1 = writeKey((byte)(size >> 8));
                        dos.writeByte(byte1);
                        int byte2 = writeKey((byte)(size & 0xFF));
                        dos.writeByte(byte2);
                        // System.out.println("l1=" + byte1 + " l2=" + byte2 + " k1"+key1+" k2="+key2);
                    } else
                        dos.writeShort(size);
                    //
                    if(connected)
                        for (int i = 0; i < data.length; i++)
                            data[i] = writeKey(data[i]);
                    dos.write(data);
                }
//                sendByteCount += (5 + data.length);
            } else {
                dos.writeShort(0);
//                sendByteCount += 5;
            }
            dos.flush();
            m.cleanup();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private byte readKey(byte b) {
        byte i = (byte)((key[curR++] & 0xff) ^ (b & 0xff));
        if (curR >= key.length)
            curR %= key.length;
        return i;
    }

    private byte writeKey(byte b) {
        byte i = (byte)((key[curW++] & 0xff) ^ (b & 0xff));
        if (curW >= key.length)
            curW %= key.length;
        return i;
    }

    @Override
    public void close() {
        try {
            if(user != null)
                user.close();
            ServerManager.disconnect(this);
            cleanNetwork();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanNetwork() {
        curR = 0;
        curW = 0;
        try {
            connected = false;
            login = false;
            if(sc != null) {
                sc.close();
                sc = null;
            }
            if (dos != null) {
                dos.close();
                dos = null;
            }
            if(dis != null) {
                dis.close();
                dis = null;
            }
            sendThread = null;
            collectorThread = null;
            System.gc();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        if(this.user != null)
            return this.user.toString();
        return "Client "+this.id;
    }
    
    public void hansakeMessage() throws IOException {
        Message ms = new Message(-27);
        DataOutputStream ds = ms.writer();
        ds.writeByte(key.length);
        ds.writeByte(key[0]);
        for(int i = 1; i < key.length; i++)
            ds.writeByte(key[i] ^ key[i-1]);
        ds.flush();
        doSendMessage(ms);
        connected = true;
        sendThread.start();
    }

    public void loginMessage(Message ms) throws IOException {
        if(this.login)
            return;
        String user = ms.reader().readUTF().trim();
        String pass = ms.reader().readUTF().trim();
        String version = ms.reader().readUTF().trim();
        ms.reader().readUTF();
        ms.reader().readUTF();
        String random = ms.reader().readUTF().trim();
        byte server = ms.reader().readByte();
        System.out.println(String.format("Client id: %d - username: %s - password: %s - version: %s - random: %s - server: %d", id, user, pass, version, random, server));
        this.versionARM = version;
        User us = User.login(this, user, pass);
        if(us != null) {
            System.out.println("Login Success!");
            this.login = true;
            this.user  = us;
            this.user.service.sendVersion();
        } else {
            System.out.println("Login Failse!");
            this.login = false;
        }
    }

    public void closeMessage() {
        if(isConnected()) {
            if(messageHandler != null)
                messageHandler.onDisconnected();
            close();
        }
    }

}
