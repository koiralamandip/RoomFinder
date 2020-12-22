package com.example.roomfinder;

public class Session {
    public static int user_id = 0;
    public static String firstname = "";
    public static String surname = "";
    public static String username = "";
    public static String phone = "";
    public static boolean isAvailable = false;
    public static boolean isLoggedOut = false;
    public static Room room;
    public static String serverIP = "192.168.1.91";

    public static void setSession(int id, String fname, String sname, String uname, String ph){
        user_id = id;
        firstname = fname;
        surname = sname;
        username = uname;
        phone = ph;
        isAvailable = true;
    }

}
