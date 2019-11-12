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

public class ModelGame extends AppCompatActivity {
    public class VerbCompare implements Comparator<Verb>
    {
        public int compare( Verb x, Verb y )
        {
            int res =(int)(x.Score - y.Score);
            return res != 0 ? res : x.infinitivG.compareTo(y.infinitivG);
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
    MediaPlayer mediaPlayerModel;
    MediaPlayer mediaPlayerInfinitiv;

    DatabaseReference dbPersonalPronomen;
    DatabaseReference dbInfinitiv;
    DatabaseReference dbModelVerbs;

    Vector<PersonalPronomen> PPS;
    PriorityBlockingQueue<Verb> InfintivVerbs;
    Vector<ModelVerb> ModelVerbs;

    String fileAddPP;
    String fileAddInfinitiv;
    String fileAddModelVerbs;
    Verb chosenInfinitiv;

    int flag = 0;
    boolean startFlag = true;
    int display = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_game);
        dbPersonalPronomen = FirebaseDatabase.getInstance().getReference("PersonalPronomen");
        dbPersonalPronomen.addListenerForSingleValueEvent(PPSEventListener);
        PPS = new Vector<>();

        next = findViewById(R.id.ModelNextButton);
        answer = findViewById(R.id.ModelAnswerButton);
        back = findViewById(R.id.ModelBackButton);
        listen = findViewById(R.id.ModelListenButton);
        green = findViewById(R.id.ModelGreenButton);
        red = findViewById(R.id.ModelRedButton);
        german = findViewById(R.id.ModelGermanText);
        hebrew = findViewById(R.id.ModelHebrewText);
        transcription = findViewById(R.id.ModelTranscriptionText);

        loadingText = findViewById(R.id.ModelLoading);
        loadingBar = findViewById(R.id.ModelBar);

        dbModelVerbs = FirebaseDatabase.getInstance().getReference("ModelVerbs");
        dbModelVerbs.addListenerForSingleValueEvent(ModelEventListener);
        ModelVerbs = new Vector<>();

        dbInfinitiv = FirebaseDatabase.getInstance().getReference("Verbs");
        dbInfinitiv.addListenerForSingleValueEvent(InfinitivEventListener);
        InfintivVerbs = new PriorityBlockingQueue<>(1,new VerbCompare());

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
                    answer.setVisibility(View.GONE);
                    hebrew.setVisibility(View.VISIBLE);
                    transcription.setVisibility(View.VISIBLE);
                    if (display > 0) {
                        green.setVisibility(View.VISIBLE);
                        red.setVisibility(View.VISIBLE);
                    }
                    display = 1;
                    flag++;
                    if (flag == 4) {
                        listen.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        listen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mediaPlayerModel.setNextMediaPlayer(mediaPlayerInfinitiv);
                mediaPlayerPP.setNextMediaPlayer(mediaPlayerModel);
                mediaPlayerPP.start();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenInfinitiv.Score +=5;
                Long ID =chosenInfinitiv.Id;
                dbInfinitiv.child(ID.toString()).child("Score").setValue(chosenInfinitiv.Score);
                InfintivVerbs.poll();
                InfintivVerbs.add(chosenInfinitiv);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chosenInfinitiv.Score +=2;
                Long ID =chosenInfinitiv.Id;
                dbInfinitiv.child(ID.toString()).child("Score").setValue(chosenInfinitiv.Score);
                InfintivVerbs.poll();
                InfintivVerbs.add(chosenInfinitiv);
                red.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                next.setText("Next Word");
            }
        });

    }
    ValueEventListener InfinitivEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Verb infinitiv = snapshot.getValue(Verb.class);
                    if(infinitiv.infinitivG.equals("brauchen"))continue;
                    InfintivVerbs.add(infinitiv);
                }
                loadingBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
                answer.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    ValueEventListener ModelEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelVerb modelVerb = snapshot.getValue(ModelVerb.class);
                    ModelVerbs.add(modelVerb);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
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
    void getVoice(Long Hform, String Modelverb, String Infinitiv,  String PP){
        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRefModel = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/PersonalPronomen/" + PP + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/PersonalPronomen/" + PP + ".m4a");
        storageRefModel.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
                            if(flag==4){
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
        StorageReference storageRefPP = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/ModelVerbs" + "/" + Modelverb + "/" + "Verb" + Hform.toString() + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/ModelVerbs" + "/" + Modelverb + "/" + "Verb" + Hform.toString() + ".m4a");
        storageRefPP.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddModelVerbs = uri.toString();
                try {
                    mediaPlayerModel = new MediaPlayer();
                    mediaPlayerModel.setDataSource(fileAddModelVerbs);
                    Log.d("my", fileAddModelVerbs);
                    mediaPlayerModel.prepareAsync();
                    mediaPlayerModel.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==4){
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
                Log.d("my", "failed to find object " + fileAddModelVerbs);
            }
        });

        StorageReference storageRefInfintiv = storage.getReferenceFromUrl("gs://applicaro-51dca.appspot.com/verben/" + Infinitiv+ "/"+"infinitiv" + ".m4a");
        System.out.println("gs://applicaro-51dca.appspot.com/verben/" + Infinitiv+ "/infinitiv" + ".m4a");
        storageRefInfintiv.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                fileAddInfinitiv=uri.toString();
                Log.d("my", fileAddInfinitiv);
                try {
                    mediaPlayerInfinitiv= new MediaPlayer();
                    mediaPlayerInfinitiv.setDataSource(fileAddInfinitiv);
                    mediaPlayerInfinitiv.prepareAsync();
                    mediaPlayerInfinitiv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            flag++;
                            if(flag==4){
                                listen.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.d("my", "failed to find object " + fileAddInfinitiv);
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
        int PPSnum = new Random().nextInt(PPS.size());
        int ModelNum = new Random().nextInt(ModelVerbs.size());
        flag=0;
        PersonalPronomen chosenPPS = PPS.elementAt(PPSnum);
        ModelVerb chosenModel = ModelVerbs.elementAt(ModelNum);
        chosenInfinitiv = InfintivVerbs.peek();

        getVoice(chosenPPS.Hform, chosenModel.infinitivG, chosenInfinitiv.infinitivG, chosenPPS.PronomenM);

        /*set german text*/
        String PPGerman = chosenPPS.PronomoenS;
        String ModelGerman = chosenModel.getGermanVerbByForm(chosenPPS.Gform);
        String InfinitivGerman =  chosenInfinitiv.infinitivG;
        String finalGerman = PPGerman + " " + ModelGerman + " " + InfinitivGerman;
        german.setText(finalGerman);

        /*set transcription text*/
        String PPtrans = chosenPPS.PronomoenG;
        String ModelTrans = chosenModel.getTransByForm(chosenPPS.Hform);
        String InfinitivTrans = chosenInfinitiv.infinitivT;
        String finalTranscription = PPtrans + " " + ModelTrans + " " + InfinitivTrans;
        transcription.setText(finalTranscription);

        /*set hebrew text*/
        String PPHebrew = chosenPPS.PronomoenH;
        String ModelHerbew = chosenModel.getHebrewAjdByForm(chosenPPS.Hform );
        String InfinitivHebrew = chosenInfinitiv.infinitivH;
        String finalHebrew = PPHebrew + " " + ModelHerbew + " " + InfinitivHebrew;
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
