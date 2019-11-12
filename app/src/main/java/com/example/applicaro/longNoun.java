package com.example.applicaro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import static android.view.View.GONE;

public class longNoun extends AppCompatActivity {
    public class NounCompare implements Comparator<Noun>
    {
        public int compare( Noun x, Noun y )
        {
            int res =(int)(x.Score - y.Score);
            return res != 0 ? res : x.German.compareTo(y.German) ;
        }
    }
    Button next;
    Button answer;
    Button back;
    Button listen;
    ImageButton green;
    ImageButton red;
    TextView german;
    TextView hebrew;
    TextView transcription;
    TextView loadingText;
    ProgressBar loadingBar;

    MediaPlayer mediaPlayerPP;
    MediaPlayer mediaPlayerVerb;
    MediaPlayer mediaPlayerNoun;
    MediaPlayer mediaPlayerArtikel;

    DatabaseReference dbPersonalPronomen;
    DatabaseReference dbLongVerbs;
    DatabaseReference dbLongNouns;

    Vector<PersonalPronomen> PPS;
    PriorityBlockingQueue<Noun> LongNouns;
    HashMap<String,Verb> LongVerbs;

    String fileAddLongPP;
    String fileAddLongVerb;
    String fileAddLongNoun;
    String fileAddLongArtikel;

    PersonalPronomen chosenPPS;
    Verb chosenVerb;
    Noun chosenNoun;
    int optnum;
    int artikelForm;
    int flag = 0; //flag is used for making sure all sounds are prepred
    boolean startFlag = true; //startFlag are for making sure first view of screen is correct

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_noun);

        PPS = new Vector<>();
        LongNouns = new PriorityBlockingQueue<>(1,new NounCompare());
        LongVerbs = new HashMap<>();

        next = findViewById(R.id.LongNextButton);
        answer = findViewById(R.id.LongAnswerButton);
        back = findViewById(R.id.LongBackButton);
        listen = findViewById(R.id.LongListenButton);
        green = findViewById(R.id.LongGreenButton);
        red = findViewById(R.id.LongRedButton);
        german = findViewById(R.id.LongGermanText);
        hebrew = findViewById(R.id.LongHebrewText);
        transcription = findViewById(R.id.LongTranscriptionText);
        loadingText = findViewById(R.id.LongLoading);
        loadingBar = findViewById(R.id.LongBar);

        dbPersonalPronomen = FirebaseDatabase.getInstance().getReference("PersonalPronomen");
        dbPersonalPronomen.addListenerForSingleValueEvent(PPSEventListener);
        dbLongNouns = FirebaseDatabase.getInstance().getReference("Nouns");
        dbLongNouns.addListenerForSingleValueEvent(NounsEventListener);
        dbLongVerbs = FirebaseDatabase.getInstance().getReference("Verbs");
        dbLongVerbs.addListenerForSingleValueEvent(LongVerbsEventListener);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNext();

            }
        });
        answer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                answer.setText("Gib mir die Antwort!");
                if (startFlag) {
                    doNext();
                    startFlag = false;
                } else {
                    back.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    answer.setVisibility(View.GONE);
                    hebrew.setVisibility(View.VISIBLE);
                    transcription.setVisibility(View.VISIBLE);
                    green.setVisibility(View.VISIBLE);
                    red.setVisibility(View.VISIBLE);
                    flag++;
                    if (flag == 5) {
                        listen.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenNoun.Score +=5;
                Long ID =chosenNoun.Id;
                dbLongNouns.child(ID.toString()).child("Score").setValue(chosenNoun.Score);
                LongNouns.poll();
                LongNouns.add(chosenNoun);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });

        red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenNoun.Score += 2;
                Long ID =chosenNoun.Id;
                dbLongNouns.child(ID.toString()).child("Score").setValue(chosenNoun.Score);
                LongNouns.poll();
                LongNouns.add(chosenNoun);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        listen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(chosenVerb.infinitivG.equals("haben")){
                    if(artikelForm==1) {
                        mediaPlayerPP.setNextMediaPlayer(mediaPlayerArtikel);
                        mediaPlayerArtikel.setNextMediaPlayer(mediaPlayerNoun);
                        mediaPlayerPP.start();
                        return;
                    }else{
                        mediaPlayerPP.setNextMediaPlayer(mediaPlayerNoun);
                        mediaPlayerPP.start();
                        return;
                    }
                }
                if(artikelForm==1){
                    mediaPlayerPP.setNextMediaPlayer(mediaPlayerVerb);
                    mediaPlayerVerb.setNextMediaPlayer(mediaPlayerArtikel);
                    mediaPlayerArtikel.setNextMediaPlayer(mediaPlayerNoun);
                    mediaPlayerPP.start();
                }else{
                    mediaPlayerPP.setNextMediaPlayer(mediaPlayerVerb);
                    mediaPlayerVerb.setNextMediaPlayer(mediaPlayerNoun);
                    mediaPlayerPP.start();
                }
            }
        });
    }

    ValueEventListener PPSEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PersonalPronomen PP = snapshot.getValue(PersonalPronomen.class);
                    PPS.add(PP);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }

    };
    ValueEventListener NounsEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Noun noun = snapshot.getValue(Noun.class);
                    LongNouns.add(noun);
                }
                loadingText.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                answer.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    ValueEventListener LongVerbsEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Verb verb = snapshot.getValue(Verb.class);
                    LongVerbs.put(verb.infinitivG,verb);
                    Log.d("my", verb.infinitivG);
                }
                Verb haben =new Verb("haben");
                LongVerbs.put("haben",haben); // add to map in order to appear later
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    void doNext(){
        flag = 0;
        int PPSnum= new Random().nextInt(PPS.size());
        artikelForm = new Random().nextInt(2); // to decide between der or ein
        chosenNoun = LongNouns.peek();
        while(chosenNoun.OptNum==0){
            Log.d("my","noun not chosen: " + chosenNoun.German);
            chosenNoun = LongNouns.poll();
        }
        Log.d("my","noun chosen: " + chosenNoun.German);
        chosenPPS = PPS.elementAt(PPSnum);
        optnum = new Random().nextInt(chosenNoun.OptNum.intValue()) + 1;
        String infVerb = chosenNoun.getOptByNum(optnum);
        Log.d("my","chosen verb:" + infVerb);
        chosenVerb = LongVerbs.get(infVerb);

        String finalGerman;
        String finalTrans ;
        String finalHebrew;

        if (infVerb.equals("haben")){
             finalGerman = chosenPPS.PronomoenS + " " + chosenPPS.HabenG + " " + getAkkByArtikel(chosenNoun.Artikel,artikelForm)+ " " + chosenNoun.German ;
            if(artikelForm == 0){ //ein case
                finalTrans = chosenPPS.HabenT + " " + chosenNoun.TranscriptionS;
                finalHebrew = chosenPPS.HabenH + " " + chosenNoun.HebrewS;
            }else{ //der case
                finalTrans  = chosenPPS.HabenT + " " +"et ha'" + chosenNoun.TranscriptionS;
                finalHebrew = chosenPPS.HabenH + " " + " " +"את ה"  + chosenNoun.HebrewS;
            }
            german.setText(finalGerman);
            transcription.setText(finalTrans);
            hebrew.setText(finalHebrew);
            doNextDisplay();

            getVoice(chosenVerb,chosenPPS,chosenNoun,"et ha1");
            return;
        }
        String artikelGerman = getTrueArtikel(chosenNoun,chosenVerb,artikelForm);
        String finalArtikel = chosenVerb.PrepositionT + Integer.toString(artikelForm);
        if(!chosenVerb.PrepositionT.equals("")) artikelForm = 1;
        getVoice(chosenVerb,chosenPPS,chosenNoun,finalArtikel);

        String PPGerman = chosenPPS.PronomoenS;
        String verbGerman = chosenVerb.getGermanByForm(chosenPPS.Gform);
        String prepGerman = chosenVerb.PrepositionG;

        String PPtrans = chosenPPS.PronomoenG;
        String verbTrans = chosenVerb.getTransByForm(chosenPPS.Hform);
        String prepTrans = chosenVerb.PrepositionT;
        String NounTrans = chosenNoun.TranscriptionS;

        String PPHebrew = chosenPPS.PronomoenH;
        String verbHerbew = chosenVerb.getHebrewByForm(chosenPPS.Hform );
        String prepHebrew = chosenVerb.PrepositionH;
        String NounHebrew = chosenNoun.HebrewS;


        finalGerman = PPGerman + " " + verbGerman + " " + prepGerman + " " + artikelGerman + " " + chosenNoun.German;
        if(artikelForm == 0){ //ein case
            finalTrans  = PPtrans + " " + verbTrans + " " + NounTrans;
            finalHebrew = PPHebrew + " " + verbHerbew + " " + NounHebrew;
        }else{ //der case
            finalTrans  = PPtrans + " " + verbTrans + " " + prepTrans + NounTrans;
            finalHebrew = PPHebrew + " " + verbHerbew + " " + prepHebrew + NounHebrew;
        }

        german.setText(finalGerman);
        transcription.setText(finalTrans);
        hebrew.setText(finalHebrew);

        doNextDisplay();
    }
    String getDatByArtikel (String Artikel, int form){
        if(Artikel.equals("der") && form == 1) return "dem";
        if(Artikel.equals("die") && form == 1) return "der";
        if(Artikel.equals("das") && form == 1) return "dem";
        if(Artikel.equals("der") && form == 0) return "einem";
        if(Artikel.equals("die") && form == 0) return "einer";
        if(Artikel.equals("das") && form == 0) return "einem";
        return "";
    }
    String getAkkByArtikel(String Artikel, int form){
        if(Artikel.equals("der") && form == 1) return "den";
        if(Artikel.equals("die") && form == 1) return "die";
        if(Artikel.equals("das") && form == 1) return "das";
        if(Artikel.equals("der") && form == 0) return "einen";
        if(Artikel.equals("die") && form == 0) return "eine";
        if(Artikel.equals("das") && form == 0) return "ein";
        return "";
    }
    String getTrueArtikel(Noun noun,Verb verb, int form){
        if(verb.Case.equals("Akk")){
            return getAkkByArtikel(noun.Artikel,form);
        }
        else{
            return getDatByArtikel(noun.Artikel,form);
        }
    }
    void getVoice(final Verb verb, PersonalPronomen pp, Noun noun, String artikel){
        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String Url = getTrueUrl(verb,pp);
        StorageReference storageRefPP = storage.getReferenceFromUrl(Url);
        System.out.println(Url);
        storageRefPP.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddLongPP = uri.toString();
                try {
                    mediaPlayerPP = new MediaPlayer();
                    mediaPlayerPP.setDataSource(fileAddLongPP);
                    Log.d("my", fileAddLongPP);
                    mediaPlayerPP.prepareAsync();
                    mediaPlayerPP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            if(verb.infinitivG.equals("haben")) flag++;
                            flag++;
                            if(flag==5){
                                listen.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("my", "failed to find object " + fileAddLongVerb);
            }
        });
        StorageReference storageRefVerb = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/verben" + "/" +verb.infinitivG + "/" + "verb" + pp.Hform.toString() + ".m4a");
        Log.d("my","gs://applicaro-51dca.appspot.com/Verben" + "/" +verb.infinitivG + "/" + "Verb" + pp.Hform.toString() + ".m4a");
        storageRefVerb.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddLongVerb=uri.toString();
                Log.d("my", fileAddLongVerb);
                try {
                    mediaPlayerVerb = new MediaPlayer();
                    mediaPlayerVerb.setDataSource(fileAddLongVerb);
                    mediaPlayerVerb.prepareAsync();
                    mediaPlayerVerb.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==5){
                                listen.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.d("my", "failed to find object " + fileAddLongVerb);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //e.printStackTrace();

            }
        });
        StorageReference storageRefNoun = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/" +"Nouns/"+ noun.Category + "/" + noun.German + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/" + noun.Category + "/" + noun.German + ".m4a");
        storageRefNoun.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddLongNoun = uri.toString();
                try {
                    mediaPlayerNoun = new MediaPlayer();
                    mediaPlayerNoun.setDataSource(fileAddLongNoun);
                    Log.d("my", fileAddLongNoun);
                    mediaPlayerNoun.prepareAsync();
                    mediaPlayerNoun.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==5){
                                listen.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("my", "failed to find object " + fileAddLongNoun);
            }
        });
        StorageReference storageRefArtikel = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/Artikel/" + artikel + ".m4a");
        Log.d("my", "gs://applicaro-51dca.appspot.com/Artikel/" + artikel + artikelForm + ".m4a");
        storageRefArtikel.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddLongArtikel = uri.toString();
                try {
                    mediaPlayerArtikel = new MediaPlayer();
                    mediaPlayerArtikel.setDataSource(fileAddLongArtikel);
                    Log.d("my", fileAddLongArtikel);
                    mediaPlayerArtikel.prepareAsync();
                    mediaPlayerArtikel.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==5){
                                listen.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("my", "failed to find object " + fileAddLongArtikel);
            }
        });
    }
    String getTrueUrl(Verb verb, PersonalPronomen pp){
        if(verb.infinitivG.equals("haben")){
            return  "gs://applicaro-51dca.appspot.com/verben" + "/haben/Form" + pp.allForm.toString() +".m4a";
        }
        else{
            return "gs://applicaro-51dca.appspot.com/PersonalPronomen/" + pp.PronomenM + ".m4a";
        }
    }
   void  doNextDisplay(){

       back.setVisibility(View.INVISIBLE);
       listen.setVisibility(View.INVISIBLE);
       next.setVisibility(View.INVISIBLE);
       answer.setVisibility(View.VISIBLE);
       hebrew.setVisibility(View.INVISIBLE);
       german.setVisibility(View.VISIBLE);
       transcription.setVisibility(View.INVISIBLE);
       green.setVisibility(View.INVISIBLE);
       red.setVisibility(View.INVISIBLE);
       next.setText("Again");
   }
}
