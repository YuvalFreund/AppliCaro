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
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

public class VerbGame extends AppCompatActivity {
    public class VerbCompare implements Comparator<Verb>
    {
        public int compare( Verb x, Verb y )
        {
            int res =(int)(x.Score - y.Score);
            return res != 0 ? res : x.infinitivG.compareTo(y.infinitivG);
        }
    }
    DatabaseReference dbVerbs;
    DatabaseReference dbPersonalPronomen;
    int flag = 0;
    int display = 0;
    boolean startFlag = true;
    Vector<PersonalPronomen> PPS;
    PriorityBlockingQueue<Verb> Verbs;

    MediaPlayer mediaPlayerPP;
    MediaPlayer mediaPlayerVerb;

    String fileAddPP;
    String fileAddVerb;
    Verb chosenVerb;

    Button next;
    Button answer;
    Button back;
    Button listen;
    ImageButton green;
    ImageButton red;
    TextView german;
    TextView hebrew;
    TextView transcription;
    TextView verbLoading;
    ProgressBar verbBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verb_game);

        dbVerbs = FirebaseDatabase.getInstance().getReference("Verbs");
        dbVerbs.addListenerForSingleValueEvent(VerbsEventListener);
        Verbs = new PriorityBlockingQueue<>(1,new VerbCompare());
        //Log.d("my",dbAdjectives.toString());

        dbPersonalPronomen = FirebaseDatabase.getInstance().getReference("PersonalPronomen");
        dbPersonalPronomen.addListenerForSingleValueEvent(PPSEventListener);
        PPS = new Vector<>();

        verbLoading = findViewById(R.id.VerbLoading);
        verbBar = findViewById(R.id.VerbBar);
        next = findViewById(R.id.VerbNextButton);
        answer = findViewById(R.id.VerbAnswerButton);
        back = findViewById(R.id.VerbBackButton);
        listen = findViewById(R.id.VerbListenButton);
        green = findViewById(R.id.VerbGreenButton);
        red = findViewById(R.id.VerbRedButton);
        german = findViewById(R.id.VerbGermanText);
        hebrew = findViewById(R.id.VerbHebrewText);
        transcription = findViewById(R.id.VerbTranscriptionText);
        next.setOnClickListener(new View.OnClickListener() {
             @Override
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
                    display = 1;
                } else {
                    back.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    answer.setVisibility(View.INVISIBLE);
                    hebrew.setVisibility(View.VISIBLE);
                    transcription.setVisibility(View.VISIBLE);
                    if (display > 0) {
                        green.setVisibility(View.VISIBLE);
                        red.setVisibility(View.VISIBLE);
                    }
                    display = 1;
                    flag++;
                    if (flag == 3) {
                        listen.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        listen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mediaPlayerPP.start();
                mediaPlayerPP.setNextMediaPlayer(mediaPlayerVerb);
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenVerb.Score +=5;
                Long ID =chosenVerb.Id;
                dbVerbs.child(ID.toString()).child("Score").setValue(chosenVerb.Score);
                Verbs.poll();
                Verbs.add(chosenVerb);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenVerb.Score += 2;
                Long ID =chosenVerb.Id;
                dbVerbs.child(ID.toString()).child("Score").setValue(chosenVerb.Score);
                Verbs.poll();
                Verbs.add(chosenVerb);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });

    }
    ValueEventListener PPSEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PersonalPronomen personalPronomen = snapshot.getValue(PersonalPronomen.class);
                    PPS.add(personalPronomen);
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    ValueEventListener VerbsEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Verb verb = snapshot.getValue(Verb.class);
                    Verbs.add(verb);
                }
                verbLoading.setVisibility(View.GONE);
                verbBar.setVisibility(View.GONE);
                answer.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    void getVoice(Verb verb,PersonalPronomen pp){
        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRefPP = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/verben" + "/" +verb.infinitivG + "/" + "verb" + pp.Hform.toString() + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/Verben" + "/" +verb.infinitivG + "/" + "Verb" + pp.Hform.toString() + ".m4a");
        storageRefPP.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddVerb = uri.toString();
                try {
                    mediaPlayerVerb = new MediaPlayer();
                    mediaPlayerVerb.setDataSource(fileAddVerb);
                    Log.d("my", fileAddVerb);
                    mediaPlayerVerb.prepareAsync();
                    mediaPlayerVerb.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==3){
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
                Log.d("my", "failed to find object " + fileAddVerb);
            }
        });
        StorageReference storageRefAdj = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/PersonalPronomen/" + pp.PronomenM + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/PersonalPronomen/" + pp.PronomenM  + ".m4a");
        storageRefAdj.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddPP=uri.toString();
                Log.d("my", fileAddPP);
                try {
                    mediaPlayerPP= new MediaPlayer();
                    mediaPlayerPP.setDataSource(fileAddPP);
                    mediaPlayerPP.prepareAsync();
                    mediaPlayerPP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==3){
                                listen.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.d("my", "failed to find object " + fileAddPP);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //e.printStackTrace();

            }
        });
    }
    void doNext(){
        flag = 0;
        int PPSnum = new Random().nextInt(PPS.size());
        PersonalPronomen chosenPPS = PPS.elementAt(PPSnum);
        chosenVerb = Verbs.peek();
        getVoice(chosenVerb,chosenPPS);

        /*set german text*/
        String PPGerman = chosenPPS.PronomoenS;
        String VerbGerman = chosenVerb.getGermanByForm(chosenPPS.Gform);
        String FinalGerman = PPGerman + " " + VerbGerman;
        german.setText(FinalGerman);

        /*set transcription text*/
        String PPtrnas = chosenPPS.PronomoenG;
        String VerbTrans = chosenVerb.getTransByForm(chosenPPS.Hform);
        String FinalTrans = PPtrnas + " " + VerbTrans;
        transcription.setText(FinalTrans);

        /*set Hebrew text*/
        String PPHebrew = chosenPPS.PronomoenH;
        String VerbHebrew = chosenVerb.getHebrewByForm(chosenPPS.Hform);
        String FinalHebrew = PPHebrew + " " + VerbHebrew;
        hebrew.setText(FinalHebrew);

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
