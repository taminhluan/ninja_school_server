package com.kitakeyos.server;

import com.kitakeyos.network.Message;
import com.kitakeyos.network.IMessageHandler;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author ASD
 */
public class MessageHandler implements IMessageHandler {

    private final ClientEntry client;

    MessageHandler(ClientEntry client) {
        this.client = client;
    }
    
    @Override
    public void onMessage(Message mss) {
        if(mss != null) {
            try {
                switch(mss.getCommand()) {
                    // Handsake
                    case -27:
                        client.hansakeMessage();
                        break;

                    case -28:
                        messageNotMap(mss);
                        break;

                    case -29:
                        messageNotLogin(mss);
                        break;

                    case -30:
                        messageSubCommand(mss);
                        break;

                    default:
                        System.out.println("CMD: " + mss.getCommand());
                        break;
                }
            } catch(IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    public void messageNotLogin(Message mss) {
        if (mss != null) {
            try {
                byte command = mss.reader().readByte();
                System.out.println("messageNotLogin: " + command);
                switch (command) {
                    case -127:
                        client.loginMessage(mss);
                        break;

                    case -125:
                        client.setClientType(mss);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void messageNotMap(Message mss) {
        if (mss != null) {
            try {
                byte command = mss.reader().readByte();
                System.out.println("messageNotMap: " + command);
                switch (command) {
                    case -126:
                        client.user.selectToChar(mss);
                        break;

                    case -125:
                        client.user.createPlayer(mss);
                        break;

                    case -122:
                        client.user.service.sendData();
                        break;

                    case -121:
                        client.user.service.sendMap();
                        break;

                    case -120:
                        client.user.service.sendSkill();
                        break;

                    case -119:
                        client.user.service.sendItem();
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void messageSubCommand(Message mss) {
        if (mss != null) {
            try {
                byte command = mss.reader().readByte();
                System.out.println("messageSubCommand: " + command);
                switch (command) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionFail() {
        System.out.println(String.format("Client %d: Kết nối thất bại!" + client.id));
    }
    
    @Override
    public void onDisconnected() {
        System.out.println(String.format("Client %d: Mất kết nối!" + client.id));
    }
    
    @Override
    public void onConnectOK() {
        System.out.println(String.format("Client %d: Kết nối thành công!" + client.id));
    }

}
