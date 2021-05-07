package com.kitakeyos.server;

import com.kitakeyos.network.Message;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class User {

    private ClientEntry client;
    public Service service;
    private ArrayList<Player> players;
    private int id;
    private String username;
    private byte lock;
    private int luong;

    public User(ClientEntry client) {
        this.client = client;
    }


    public static User login(ClientEntry cl, String username, String password) throws IOException {
        User us = new User(cl);
        us.service = new Service(cl);
        try {
            if (username.equals("-1") && password.equals("12345")) {
                us.service.startOKDlg("Chức năng tạm đóng!");
                return null;
            }
            ResultSet resultSet = SQLManager.stat.executeQuery("SELECT * FROM `user` WHERE `username` = '" + username + "' AND `password` = '" + password + "' LIMIT 1");
            if (resultSet.first()) {
                if (us.lock == 1) {
                    us.service.startOKDlg("Tài khoản đã bị khoá! Vui lòng liên hệ admin để biết thêm chi tiết");
                    return null;
                }
                us.id = resultSet.getInt("id");
                us.username = resultSet.getString("username");
                us.lock = resultSet.getByte("lock");
                us.luong = resultSet.getInt("luong");
                resultSet.close();
                us.players = new ArrayList<>();
                resultSet = SQLManager.stat.executeQuery("SELECT * FROM `player` WHERE `user_id` = " + us.id + " LIMIT 3 ");
                while (resultSet.next()) {
                    Player player = new Player();
                    player.id = resultSet.getInt("id");
                    player.name = resultSet.getString("name");
                    player.gender = resultSet.getByte("gender");
                    player.school = resultSet.getString("school");
                    player.vocation = resultSet.getByte("vocation");
                    player.level = resultSet.getInt("level");
                    player.head = resultSet.getShort("head");
                    player.weapon = resultSet.getShort("weapon");
                    player.body = resultSet.getShort("body");
                    player.leg = resultSet.getShort("leg");
                    player.xu = resultSet.getInt("xu");
                    player.yen = resultSet.getInt("yen");
                    us.players.add(player);
                }
                us.service.selectChar(us.players);
                return us;
            } else {
                resultSet.close();
                us.service.startOKDlg("Tài khoản hoặc mật khẩu không chính xác!");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createPlayer(Message ms) throws IOException {
        try {
            String username = ms.reader().readUTF();
            byte gender = ms.reader().readByte();
            byte head = ms.reader().readByte();
            if (username.length() < 6 || username.length() > 20) {
                service.startOKDlg("Tên tài khoản chỉ cho phép từ 6 đến 20 kí tự!");
                return;
            }
            ResultSet check = SQLManager.stat.executeQuery("SELECT * FROM `player` WHERE `name` = '" + username + "';");
            if (check.last()) {
                if (check.getRow() > 0) {
                    service.startOKDlg("Tên tài khoản đã tồn tại!");
                    return;
                }
            }
            SQLManager.stat.executeUpdate("INSERT INTO player(`user_id`, `name`, `gender`, `head`) values (" + this.id + ", '" + username + "', " + gender + ", " + head + ")");
            ResultSet resultSet = SQLManager.stat.executeQuery("SELECT * FROM `player` WHERE `user_id` = " + this.id + " LIMIT 3;");
            players = new ArrayList<>();
            while (resultSet.next()) {
                Player player = new Player();
                player.id = resultSet.getInt("id");
                player.name = resultSet.getString("name");
                player.gender = resultSet.getByte("gender");
                player.school = resultSet.getString("school");
                player.vocation = resultSet.getByte("vocation");
                player.level = resultSet.getInt("level");
                player.head = resultSet.getShort("head");
                player.weapon = resultSet.getShort("weapon");
                player.body = resultSet.getShort("body");
                player.leg = resultSet.getShort("leg");
                player.xu = resultSet.getInt("xu");
                player.yen = resultSet.getInt("yen");
                players.add(player);
            }
            service.selectChar(players);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            service.startOKDlg("Tạo nhân vật thất bại!");
        }
    }

    public void selectToChar(Message ms) throws IOException {
        String username = ms.reader().readUTF();
        for (Player pl : players) {
            if (pl.name.equals(username)) {
                this.service.sendMap();
                this.service.sendInfo(pl);
                return;
            }
        }
    }

    public void close() {

    }

    @Override
    public String toString() {
        return this.username;
    }
}

class Player {
    protected int id;
    protected String name;
    protected byte gender;
    protected String school;
    protected byte vocation;
    protected int level;
    protected short head;
    protected short weapon;
    protected short body;
    protected short leg;
    protected int xu;
    protected int yen;
}