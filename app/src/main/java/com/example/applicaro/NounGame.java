package com.example.applicaro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.VoiceInteractor;
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
import java.util.concurrent.PriorityBlockingQueue;

import static android.view.View.*;


public class NounGame extends AppCompatActivity {

    public class NounCompare implements Comparator<Noun>
    {
        public int compare( Noun x, Noun y )
        {
            Log.d("my","in comp");
            int res =(int)(x.Score - y.Score);
            return res != 0 ? res : x.German.compareTo(y.German) ;
        }
    }
    Button answer;
    Button back;
    Button listen;
    ImageButton green;
    ImageButton red;
    TextView german;
    TextView hebrew;
    TextView transcription;
    MediaPlayer mediaPlayerNoun;
    DatabaseReference dbNouns;
    PriorityBlockingQueue<Noun> Nouns;
    TextView  NounLoading;
    ProgressBar NounBar;

    String fileAddNoun;
    Noun chosenNoun;
    int flag = 0;
    int display = 0;
    boolean startFlag= true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noun_game);
        dbNouns = FirebaseDatabase.getInstance().getReference("Nouns");
        dbNouns.addListenerForSingleValueEvent(NounsEventListener);
        Log.d("my",dbNouns.toString());
        Nouns = new PriorityBlockingQueue<>(1,new NounCompare());

        answer = findViewById(R.id.NounAnswerButton);
        back = findViewById(R.id.NounBackButton);
        listen = findViewById(R.id.NounListenButton);
        green = findViewById(R.id.NounGreenButton);
        red = findViewById(R.id.NounRedButton);
        german = findViewById(R.id.NounGermanText);
        hebrew = findViewById(R.id.NounHebrewText);
        transcription = findViewById(R.id.NounTranscriptionText);
        NounLoading =findViewById(R.id.NounLoading);
        NounBar = findViewById(R.id.NounBar);

        answer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                answer.setText("Gib mir die Antwort!");
                if (startFlag) {
                    doNext();
                    startFlag = false;
                    display = 1;
                } else {
                    back.setVisibility(View.VISIBLE);
                   // next.setVisibility(View.VISIBLE);
                    answer.setVisibility(View.INVISIBLE);
                    hebrew.setVisibility(View.VISIBLE);
                    transcription.setVisibility(View.VISIBLE);
                    if (display > 0) {
                        green.setVisibility(View.VISIBLE);
                        red.setVisibility(View.VISIBLE);
                    }
                    display = 1;
                    flag++;
                    if (flag == 2) {
                        listen.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        listen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mediaPlayerNoun.start();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenNoun.Score +=5;
                Long ID =chosenNoun.Id;
                dbNouns.child(ID.toString()).child("Score").setValue(chosenNoun.Score);
                Nouns.poll();
                Nouns.add(chosenNoun);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                //next.setText("Next Word");
                doNext();
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenNoun.Score += 2;
                Long ID =chosenNoun.Id;
                dbNouns.child(ID.toString()).child("Score").setValue(chosenNoun.Score);
                Nouns.poll();
                Nouns.add(chosenNoun);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                //next.setText("Next Word");
                doNext();
            }
        });
    }
    String assignMark (String val){
        if(val.equals("F")) return "♀";
        if(val.equals("M")) return "♂";
         return "";
    }
    ValueEventListener NounsEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.d("my", snapshot.toString());
                    Noun noun = snapshot.getValue(Noun.class);
                    Log.d("my", noun.Artikel);
                    Nouns.add(noun);
                }
                NounBar.setVisibility(View.GONE);
                NounLoading.setVisibility(GONE);
                answer.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    void getVoice(Noun noun){
        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRefPP = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/" +"Nouns/"+ noun.Category + "/" + noun.German + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/" + noun.Category + "/" + noun.German + ".m4a");
        storageRefPP.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddNoun = uri.toString();
                try {
                    mediaPlayerNoun = new MediaPlayer();
                    mediaPlayerNoun.setDataSource(fileAddNoun);
                    Log.d("my", fileAddNoun);
                    mediaPlayerNoun.prepareAsync();
                    mediaPlayerNoun.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==2){
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
                Log.d("my", "failed to find object " + fileAddNoun);
            }
        });
    }
    void doNext(){
        flag = 0;
        chosenNoun = Nouns.peek();
        getVoice(chosenNoun);

        /*set german text*/
        String GermanNoun = chosenNoun.German;
        String GermanArtikel = chosenNoun.Artikel;
        String FinalGerman = GermanArtikel + " " + GermanNoun;
        german.setText(FinalGerman);

        /*set transcription text*/
        String TransNoun =chosenNoun.TranscriptionS;
        transcription.setText(TransNoun);

        /*set hebrew text*/
        String HebrewNoun = chosenNoun.HebrewS;
        String sign =assignMark(chosenNoun.hebrewGender);
        String HebrewFinal = HebrewNoun + " " + sign;
        hebrew.setText(HebrewFinal);

        back.setVisibility(View.INVISIBLE);
        listen.setVisibility(View.INVISIBLE);
        //next.setVisibility(View.INVISIBLE);
        answer.setVisibility(View.VISIBLE);
        hebrew.setVisibility(View.INVISIBLE);
        german.setVisibility(View.VISIBLE);
        transcription.setVisibility(View.INVISIBLE);
        green.setVisibility(View.INVISIBLE);
        red.setVisibility(View.INVISIBLE);
        //next.setText("Again");
    }
}
