package com.example.applicaro;

public class Noun {
   public String German;
   public String Artikel;
   public String TranscriptionS;
   public String TranscriptionP;
   public String HebrewP;
   public String HebrewS;
   public String hebrewGender;
   public String Category;
   public String opt1;
   public String opt2;
   public String opt3;
   public Long OptNum;
   public Long Score;
   public Long Id;

   public Noun(){

   }
   public String getOptByNum(int num){
       if(num==1) return opt1;
       if(num==2) return opt2;
       if(num==3) return opt3;
       return "";
   }
   public Noun(String german, String artikel, String transcriptionS, String transcriptionP, String hebrewP, String hebrewS, String hebrewGender, String category, String opt1, String opt2, String opt3, Long optNum, Long score, Long id) {
       German = german;
       Artikel = artikel;
       TranscriptionS = transcriptionS;
       TranscriptionP = transcriptionP;
       HebrewP = hebrewP;
       HebrewS = hebrewS;
       this.hebrewGender = hebrewGender;
       Category = category;
       this.opt1 = opt1;
       this.opt2 = opt2;
       this.opt3 = opt3;
       OptNum = optNum;
       Score = score;
       Id = id;
   }
}
