package com.kitakeyos.server;

import com.kitakeyos.network.Message;
import java.io.*;
import java.util.ArrayList;

public class Service {

    private ClientEntry client;

    public Service(ClientEntry client) {
        this.client = client;
    }

    public void selectChar(ArrayList<Player> players) throws IOException {
        Message ms = messageNotMap((byte) -126);
        DataOutputStream ds = ms.writer();
        ds.writeByte(players.size());
        for (Player player : players) {
            ds.writeByte(player.gender); // gender
            ds.writeUTF(player.name); // name
            ds.writeUTF(player.school); // phai
            ds.writeByte((byte) player.level);// Level
            ds.writeShort(player.head); // Head
            ds.writeShort(player.weapon); // Weapon
            ds.writeShort(player.body); // body
            ds.writeShort(player.leg); // leg
        }
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendZone() throws IOException {
        Message ms = new Message(-18);
        DataOutputStream ds = ms.writer();
        ds.writeByte(32);
        ds.writeByte(1);
        ds.writeByte(1);
        ds.writeByte(0);
        ds.writeUTF("Làng chài");
        ds.writeByte(20);
        ds.writeShort(100);//X
        ds.writeShort(10); // Y
        ds.writeByte(0);// vGo
        ds.writeByte(0);// mob
        ds.writeByte(2); // bu nhin
        for (int i = 0; i < 2; i++) {
            ds.writeUTF("Bù nhìn");
            ds.writeShort(i * 100);
            ds.writeShort(10);
        }
        ds.writeByte(0);// npc
        ds.writeByte(0);// item
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendInfo(Player pl) throws IOException {
        Message ms = messageSubCommand((byte) -127);
        DataOutputStream ds = ms.writer();
        ds.writeInt(pl.id);
        ds.writeUTF("");
        ds.writeByte(1);//taskId
        ds.writeByte(pl.gender);
        ds.writeShort(pl.head);
        ds.writeByte(10);
        ds.writeUTF(pl.name);
        ds.writeByte(0);//pk
        ds.writeByte(0);//type pk
        ds.writeInt(100000); // maxHP
        ds.writeInt(99999); // hp
        ds.writeInt(100000); // maxMP
        ds.writeInt(99999); // mp
        ds.writeLong(100000); // exp
        ds.writeLong(99999); //exp dowm
        ds.writeShort(0); // eff5buff
        ds.writeShort(0); // eff5buff
        ds.writeByte(1);// nclass
        ds.writeShort(1999); //point
        ds.writeShort(1000); //potential 0
        ds.writeShort(1000); //potential 1
        ds.writeInt(1000); //potential 2
        ds.writeInt(1000); //potential 3
        ds.writeShort(888); // spoint
        ds.writeByte(0);// skill
        ds.writeInt(9999999);//yen
        ds.writeInt(9999999);//xu
        ds.writeInt(9999999);//luong
        ds.writeByte(0);//bag

        // trang bi
        for (int i = 0; i < 16; i++) {
            ds.writeShort(-1);
        }
        ds.writeBoolean(false);
        ds.writeBoolean(false);
        ds.writeShort(-1);
        ds.writeShort(-1);
        ds.writeShort(-1);
        ds.writeShort(-1);
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendCharInfo(Player pl) throws IOException {
        Message ms = new Message(3);
        DataOutputStream ds = ms.writer();
        ds.writeInt(pl.id);
        ds.writeUTF("");
        ds.writeBoolean(false);
        ds.writeByte(0);// pk
        ds.writeByte(0);// nclass
        ds.writeShort(pl.head);
        ds.writeUTF(pl.name);
        ds.writeInt(10000);
        ds.writeInt(10000);
        ds.writeByte(pl.level);
        ds.writeShort(pl.weapon);
        ds.writeShort(pl.body);
        ds.writeShort(pl.leg);
        ds.writeByte(0);
        ds.writeShort(100);// X
        ds.writeShort(10);// Y
        ds.writeShort(0);
        ds.writeShort(0);
        ds.writeByte(0);
        ds.writeBoolean(true); // human
        ds.writeBoolean(false); // nhan ban
        ds.writeShort(pl.head);
        ds.writeShort(pl.weapon);
        ds.writeShort(pl.body);
        ds.writeShort(pl.leg);
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendVersion() throws IOException {
        Message ms = messageNotMap((byte) -123);
        DataOutputStream ds = ms.writer();
        ds.writeByte(ServerManager.dataVersion);
        ds.writeByte(ServerManager.mapVersion);
        ds.writeByte(ServerManager.skillVersion);
        ds.writeByte(ServerManager.itemVersion);
        ds.writeByte(0);
        ds.writeByte(0);
        ds.writeByte(0);
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendData() throws IOException {
        Message ms = messageNotMap((byte) -122);
        DataOutputStream ds = ms.writer();
        ds.write(NinjaSchool.getFile("cache/data"));
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendMap() throws IOException {
        Message ms = messageNotMap((byte) -121);
        DataOutputStream ds = ms.writer();
        ds.write(NinjaSchool.getFile("cache/map"));
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendSkill() throws IOException {
        Message ms = messageNotMap((byte) -120);
        DataOutputStream ds = ms.writer();
        ds.write(NinjaSchool.getFile("cache/skill"));
        ds.flush();
        client.sendMessage(ms);
    }

    public void sendItem() throws IOException {
        Message ms = messageNotMap((byte) -119);
        DataOutputStream ds = ms.writer();
        ds.write(NinjaSchool.getFile("cache/item"));
        ds.flush();
        client.sendMessage(ms);
    }

    public void startOKDlg(String text) throws IOException {
        Message ms = new Message(-26);
        DataOutputStream ds = ms.writer();
        ds.writeUTF(text);
        ds.flush();
        client.sendMessage(ms);
    }

    public void addInfo(String text) throws IOException {
        Message ms = new Message(-25);
        DataOutputStream ds = ms.writer();
        ds.writeUTF(text);
        ds.flush();
        client.sendMessage(ms);
    }

    private Message messageNotLogin(byte command) throws IOException {
        Message ms = new Message(-29);
        ms.writer().writeByte(command);
        return ms;
    }

    private Message messageNotMap(byte command) throws IOException {
        Message ms = new Message(-28);
        ms.writer().writeByte(command);
        return ms;
    }

    private Message messageSubCommand(byte command) throws IOException {
        Message ms = new Message(-30);
        ms.writer().writeByte(command);
        return ms;
    }
}
