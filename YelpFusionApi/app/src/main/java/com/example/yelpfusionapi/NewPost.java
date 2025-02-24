package com.example.yelpfusionapi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseRegistrar;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class NewPost extends AppCompatActivity {

    DatabaseReference ref;
    Spinner time1;
    Spinner am_pm1;
    Spinner time2;
    Spinner am_pm2;
    CheckBox cb_male;
    CheckBox cb_female;
    Spinner age1;
    Spinner age2;
    Spinner country;
    Spinner language;
    EditText note;
    Button new_post;
    //set range of the ages
    Integer age_from = new Integer(18);
    Integer age_to = new Integer(40);
    String gender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        time1 = findViewById(R.id.time1);
        time2 = findViewById(R.id.time2);
        am_pm1 = findViewById(R.id.am_pm1);
        am_pm2 = findViewById(R.id.am_pm2);
        country = findViewById(R.id.country);
        language = findViewById(R.id.language);
        note = findViewById(R.id.note);

        List age_list = new ArrayList<Integer>();
        for (int i = age_from; i <= age_to; i++){
            age_list.add(Integer.toString(i));
        }

        ArrayAdapter<Integer> age_adapter = new ArrayAdapter<Integer>(
                this, android.R.layout.simple_spinner_item, age_list);
        age_adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

        age1 = findViewById(R.id.age1);
        age2 = findViewById(R.id.age2);
        age1.setAdapter(age_adapter);
        age2.setAdapter(age_adapter);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference("Posts");
        new_post = findViewById(R.id.btn_new_post);

        final String res_id = getIntent().getStringExtra("resID");

        cb_male = findViewById(R.id.cb_male);
        cb_female = findViewById(R.id.cb_female);

        new_post.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (cb_male.isChecked())gender = "male";
                if (cb_female.isChecked())gender = "female";
                DatabaseReference ref_id = ref.child(res_id);
                DatabaseReference ref_post = ref_id.push();
                ref_post.setValue(new PostModel("peter",
                        "https://i.imgur.com/6gNZJPD.jpg",
                        gender,
                        country.getSelectedItem().toString(),
                        language.getSelectedItem().toString(),
                        time1.getSelectedItem().toString() + am_pm1.getSelectedItem().toString(),
                        time2.getSelectedItem().toString() + am_pm2.getSelectedItem().toString(),
                        note.getText().toString()));
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
        });
    }
}
