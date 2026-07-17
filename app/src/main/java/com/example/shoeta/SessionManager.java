package com.example.shoeta;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF    = "PirbariSession";
    private static final String LOGGED  = "logged_in";
    private static final String KEY_ID  = "user_id";
    private static final String KEY_NAME= "user_name";
    private static final String KEY_TYPE= "user_type";
    private static final String KEY_PHONE="user_phone";
    private static final String KEY_PROPRIETOR = "proprietor_name";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context ctx) {
        pref   = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(int id, String name, String type, String phone,String proprietorName) {
        editor.putBoolean(LOGGED, true);
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_TYPE, type);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_PROPRIETOR, proprietorName == null ? "" : proprietorName);
        editor.apply();
    }

    public void logout() { editor.clear(); editor.apply(); }

    public boolean isLoggedIn() { return pref.getBoolean(LOGGED, false); }
    public int     getUserId()  { return pref.getInt(KEY_ID, -1); }
    public String  getUserName(){ return pref.getString(KEY_NAME, ""); }
    public String  getUserType(){ return pref.getString(KEY_TYPE, ""); }
    public String  getUserPhone(){ return pref.getString(KEY_PHONE, ""); }
    public String  getProprietorName(){ return pref.getString(KEY_PROPRIETOR, ""); }

    public boolean isAdmin()   { return "admin".equals(getUserType()); }
    public boolean isFactory() { return "factory".equals(getUserType()); }
    public boolean isWorker()  { return "worker".equals(getUserType()); }
}