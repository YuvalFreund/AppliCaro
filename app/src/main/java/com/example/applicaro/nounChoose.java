package com.example.applicaro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class nounChoose extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noun_choose);
        Button only = findViewById(R.id.onlyNounBtn);
        Button sentence = findViewById(R.id.sentenceBtn);
        Button back = findViewById(R.id.nounChooseBackBtn);
        only.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent only = new Intent(nounChoose.this,NounGame.class);
                startActivity(only);
            }
        });
        sentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent only = new Intent(nounChoose.this,longNoun.class);
                startActivity(only);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
