package com.example.applicaro;

public class PersonalPronomen {
     public String PronomenM;
     public String PronomoenS;
     public String PronomoenG;
     public String PronomoenH;
     public Long Hform;
     public Long Gform;
     public String SeinForm;
     public Long allForm;
     public String AkkH;
     public String AkkT;
     public String AkkG;
     public String DatH;
     public String DatT;
     public String DatG;
     public String HabenH;
     public String HabenT;
     public String HabenG;

    public PersonalPronomen(){

    }

    public PersonalPronomen(String pronomenM, String pronomoenS, String pronomoenG, String pronomoenH, Long hform, Long gform, String seinForm, Long allForm, String akkH, String akkT, String akkG, String datH, String datT, String datG, String habenH, String habenT, String habenG) {
        PronomenM = pronomenM;
        PronomoenS = pronomoenS;
        PronomoenG = pronomoenG;
        PronomoenH = pronomoenH;
        Hform = hform;
        Gform = gform;
        SeinForm = seinForm;
        this.allForm = allForm;
        AkkH = akkH;
        AkkT = akkT;
        AkkG = akkG;
        DatH = datH;
        DatT = datT;
        DatG = datG;
        HabenH = habenH;
        HabenT = habenT;
        HabenG = habenG;
    }
}
