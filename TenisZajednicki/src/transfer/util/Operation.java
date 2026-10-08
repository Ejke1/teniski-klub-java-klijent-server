/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transfer.util;

/**
 *
 * @author Korisnik
 */
public interface Operation {

    public static final int LOGIN = 0;
    public static final int LOGOUT = 1;

    public static final int ADD_CLAN = 2;
    public static final int DELETE_CLAN = 3;
    public static final int UPDATE_CLAN = 4;
    public static final int GET_ALL_CLAN = 5;

    public static final int ADD_GRUPA = 6;
    public static final int DELETE_GRUPA = 7;
    public static final int UPDATE_GRUPA = 8;
    public static final int GET_ALL_GRUPA = 9;

    public static final int ADD_TRENER = 10;
    public static final int DELETE_TRENER = 11;
    public static final int UPDATE_TRENER = 12;
    public static final int GET_ALL_TRENER = 13;

    public static final int GET_ALL_KATEGORIJA = 14;

}
