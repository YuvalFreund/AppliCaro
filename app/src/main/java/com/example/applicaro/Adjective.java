package com.example.applicaro;

import android.util.Log;

public class Adjective {
    public String AdjG;
    public String Adj1H;
    public String Adj1G;
    public String Adj2H;
    public String Adj2G;
    public String Adj3H;
    public String Adj3G;
    public String Adj4H;
    public String Adj4G;
    public Long Score;
    public Long Id;
    public Adjective(){

    }

    public void print(){
        String res = AdjG + " " + Adj1G + " " +Adj2G;
        Log.d("my",res);
    }
    public String getGermanAjdByForm(Long form){
        if(form==1) return Adj1G;
        if(form==2) return Adj2G;
        if(form==3) return Adj3G;
        if(form==4) return Adj4G;
        //Log.d("my","DIDNT GET VALUE");

        return "";
    }
    public String getHebrewAjdByForm(Long form){
        if(form==1) return Adj1H;
        if(form==2) return Adj2H;
        if(form==3) return Adj3H;
        if(form==4) return Adj4H;
        return "";
    }


    public Adjective(String adjG, String adj1H, String adj1G, String adj2H, String adj2G, String adj3H, String adj3G, String adj4H, String adj4G, Long score, Long ID) {
        AdjG = adjG;
        Adj1H = adj1H;
        Adj1G = adj1G;
        Adj2H = adj2H;
        Adj2G = adj2G;
        Adj3H = adj3H;
        Adj3G = adj3G;
        Adj4H = adj4H;
        Adj4G = adj4G;
        Score = score;
        this.Id = ID;
    }
}
