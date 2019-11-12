package com.example.applicaro;

public class Verb {
    public String infinitivG;
    public String infinitivH;
    public String verb1G;
    public String verb1H;
    public String verb2G;
    public String verb2H;
    public String verb3G;
    public String verb3H;
    public String verb4G;
    public String verb4H;
    public String Reflexiv;
    public String Gform1;
    public String Gform2;
    public String Gform3;
    public String Gform4;
    public String Gform5;
    public String Gform6;
    public String infinitivT;
    public Long Id;
    public Long Score;
    public String PrepositionH;
    public String PrepositionT;
    public String PrepositionG;
    public String Case;

    public Verb(){

    }

    public Verb(String infinitivG) {
        this.infinitivG = infinitivG;
    }

    String getHebrewByForm(Long form){
        if (form==1) return verb1H;
        if (form==2) return verb2H;
        if (form==3) return verb3H;
        if (form==4) return verb4H;
        return "";
    }
    String getTransByForm(Long form){
        if (form==1) return verb1G;
        if (form==2) return verb2G;
        if (form==3) return verb3G;
        if (form==4) return verb4G;
        return "";
    }
    String getGermanByForm(Long form){
        if (form==1) return Gform1;
        if (form==2) return Gform2;
        if (form==3) return Gform3;
        if (form==4) return Gform4;
        if (form==5) return Gform5;
        if (form==6) return Gform6;
        return "";
    }

    public Verb(String infinitivG, String infinitivH, String verb1G, String verb1H, String verb2G, String verb2H, String verb3G, String verb3H, String verb4G, String verb4H, String reflexiv, String gform1, String gform2, String gform3, String gform4, String gform5, String gform6, String infinitivT, Long id, Long score, String prepositionH, String prepositionT, String prepositionG, String aCase) {
        this.infinitivG = infinitivG;
        this.infinitivH = infinitivH;
        this.verb1G = verb1G;
        this.verb1H = verb1H;
        this.verb2G = verb2G;
        this.verb2H = verb2H;
        this.verb3G = verb3G;
        this.verb3H = verb3H;
        this.verb4G = verb4G;
        this.verb4H = verb4H;
        Reflexiv = reflexiv;
        Gform1 = gform1;
        Gform2 = gform2;
        Gform3 = gform3;
        Gform4 = gform4;
        Gform5 = gform5;
        Gform6 = gform6;
        this.infinitivT = infinitivT;
        Id = id;
        Score = score;
        PrepositionH = prepositionH;
        PrepositionT = prepositionT;
        PrepositionG = prepositionG;
        Case = aCase;
    }
}
 