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
import java.util.Comparator;



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
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

public class AdjGame extends AppCompatActivity {
    public class AdjCompare implements Comparator<Adjective>
    {
        public int compare( Adjective x, Adjective y )
        {
            int res =(int)(x.Score - y.Score);
            return res != 0 ? res : x.AdjG.compareTo(y.AdjG) ;
        }
    }
    DatabaseReference dbAdjectives;
    DatabaseReference dbPersonalPronomen;
    int flag = 0;
    boolean startFlag = true;
    int display = 0;
    Vector<PersonalPronomen> PPS;
    PriorityBlockingQueue<Adjective> Adjectives;
    MediaPlayer mediaPlayerPP;
    MediaPlayer mediaPlayerAdj;

    String fileAddPP;
    String fileAddAdj;

    Button next;
    Button answer;
    Button back;
    Button listen;
    ImageButton green;
    ImageButton red;
    TextView german;
    TextView hebrew;
    TextView transcription;
    TextView  AdjLoading;
    ProgressBar    AdjBar;
    Adjective chosenAdj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adj_game);
        dbAdjectives = FirebaseDatabase.getInstance().getReference("Adjectives");
        dbAdjectives.addListenerForSingleValueEvent(adjectivesEventListener);
        Adjectives = new PriorityBlockingQueue<>(1,new AdjCompare());
        //Log.d("my",dbAdjectives.toString());

        dbPersonalPronomen = FirebaseDatabase.getInstance().getReference("PersonalPronomen");
        dbPersonalPronomen.addListenerForSingleValueEvent(PPSEventListener);
        PPS = new Vector<>();

        AdjBar = findViewById(R.id.AdjBar);
        AdjLoading = findViewById(R.id.AdjLoading);
        next = findViewById(R.id.AdjNextButton);
        answer = findViewById(R.id.AdjAnswerButton);
        back = findViewById(R.id.AdjBackButton);
        listen = findViewById(R.id.AdjListenButton);
        green = findViewById(R.id.AdjGreenButton);
        red = findViewById(R.id.AdjRedButton);
        german = findViewById(R.id.AdjGermanText);
        hebrew = findViewById(R.id.AdjHebrewText);
        transcription = findViewById(R.id.AdjTranscriptionText);
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
                mediaPlayerPP.setNextMediaPlayer(mediaPlayerAdj);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenAdj.Score +=5;
                Long ID =chosenAdj.Id;
                dbAdjectives.child(ID.toString()).child("Score").setValue(chosenAdj.Score);
                Adjectives.poll();
                Adjectives.add(chosenAdj);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenAdj.Score += 2;
                Long ID =chosenAdj.Id;
                dbAdjectives.child(ID.toString()).child("Score").setValue(chosenAdj.Score);
                Adjectives.poll();
                Adjectives.add(chosenAdj);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });
    }


    ValueEventListener adjectivesEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Adjective adjective = snapshot.getValue(Adjective.class);
                    Adjectives.add(adjective);
                }
                AdjBar.setVisibility(View.GONE);
                AdjLoading.setVisibility(View.GONE);
                answer.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
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
    void getVoice(Long form, String Adj, String PP){
        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRefPP = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/adjektiv" + "/" + Adj + "/" + "Adj" + form.toString() + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/adjektiv" + "/" + Adj + "/" + "Adj" + form.toString() + ".m4a");
        storageRefPP.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddAdj = uri.toString();
                try {
                    mediaPlayerAdj = new MediaPlayer();
                    mediaPlayerAdj.setDataSource(fileAddAdj);
                    Log.d("my", fileAddAdj);
                    mediaPlayerAdj.prepareAsync();
                    mediaPlayerAdj.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
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
                Log.d("my", "failed to find object " + fileAddAdj);
            }
        });
        StorageReference storageRefAdj = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/PersonalPronomen/" + PP + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/PersonalPronomen/" + PP + ".m4a");
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
        chosenAdj = Adjectives.peek();
        Log.d("my",chosenAdj.Adj1G);
        Log.d("my",chosenAdj.Adj2G);
        getVoice(chosenPPS.Hform,chosenAdj.AdjG,chosenPPS.PronomenM);

        /*set german text*/
        String PPGerman = chosenPPS.PronomoenS;
        String PPSein = chosenPPS.SeinForm;
        String AdjGerman = chosenAdj.AdjG;
        String finalGerman = PPGerman + " " + PPSein + " " + AdjGerman;
        german.setText(finalGerman);

        /*set transcription text*/
        String PPtranscription = chosenPPS.PronomoenG;
        String AdjTranscription = chosenAdj.getGermanAjdByForm(chosenPPS.Hform);
        String finalTranscription = PPtranscription + " " + AdjTranscription;
        transcription.setText(finalTranscription);

        /*set hebrew text*/
        String PPHebrew = chosenPPS.PronomoenH;
        String AdjHerbew = chosenAdj.getHebrewAjdByForm(chosenPPS.Hform );
        String finalHebrew = PPHebrew + " " + AdjHerbew;
        hebrew.setText(finalHebrew);



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

