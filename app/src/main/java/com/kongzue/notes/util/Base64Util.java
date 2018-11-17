package com.kongzue.notes.util;

import android.util.Base64;

public class Base64Util {

    public static String decode(String b){
        if (b.isEmpty()){
            return "";
        }
        return new String(Base64.decode(b, Base64.NO_WRAP));
    }
    
    public static String encode(String s){
        return new String(Base64.encode(s.getBytes(), Base64.NO_WRAP));
    }
}
