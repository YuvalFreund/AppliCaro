package com.example.applicaro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class choose extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        Button Adjectivs = findViewById(R.id.AdjBtn);
        Button model = findViewById(R.id.ModelBtn);
        Button Noun= findViewById(R.id.NounsBtn);
        Button Verb = findViewById(R.id.VerbBtn);
        Adjectivs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent game = new Intent(choose.this, AdjGame.class);
                startActivity(game);
            }
        });
        model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent model = new Intent(choose.this,ModelGame.class);
                startActivity(model);
            }
        });
        Noun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent noun = new Intent(choose.this,nounChoose.class);
                startActivity(noun);
            }
        });
        Verb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent noun = new Intent(choose.this,VerbGame.class);
                startActivity(noun);
            }
        });

    }
}
