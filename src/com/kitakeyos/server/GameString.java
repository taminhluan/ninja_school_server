/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kitakeyos.server;

/**
 *
 * @author ASD
 */
public class GameString {

    private static GameString gameString = new GameString();

    public static final String AUTHOR = "Hoàng Hữu Dũng";

    public static String getString(String name, String... data) {
        try {
            String text = gameString.getClass().getField(name).get(null).toString();
            return String.format(text, data);
        } catch (Exception e) {
        }
        return "Không tìm thấy nội dung!";
    }

    
}
