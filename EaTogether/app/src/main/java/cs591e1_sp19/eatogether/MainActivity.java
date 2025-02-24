package cs591e1_sp19.eatogether;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText email,password,name;
    private Button registerButton;
    private Button loginButton;
    private FirebaseAuth firebaseAuth;

    //we'll first sign one picture for users, since we don't have a local database, so for speed consideration, we just hard-coding here,
    //of our future job is to build a local cache so such thing can be much faster
    final private String img = "https://firebasestorage.googleapis.com/v0/b/eatogether-cs591.appspot.com/o/profile_img-01.png?alt=media&token=ef833632-1a12-47cf-aecf-a4504abe9c02";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.userEmail);
        name = findViewById(R.id.userName);
        password = findViewById(R.id.userPassword);
        registerButton = findViewById(R.id.userRegisterButton);
        loginButton = findViewById(R.id.userLoginButton);

        firebaseAuth = FirebaseAuth.getInstance();

        // we would first check if all this input time is legal
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailS = email.getText().toString();
                final String nameS = name.getText().toString();
                final String passwordS = password.getText().toString();

                if(TextUtils.isEmpty(emailS)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(nameS)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(passwordS)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                }

                if(passwordS.length()<6){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
                }

                //we would use firebaseAuth here so it's more secure
                firebaseAuth.createUserWithEmailAndPassword(emailS,passwordS)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    Toast.makeText(getApplicationContext(), "register successful!", Toast.LENGTH_SHORT).show();
                                    addUser(emailS, nameS, passwordS);
                                    finish();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"E-mail or password is wrong",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });
    }

    //we will asign our new user with original rating with 5.0, and a primary avatar, which he/she can change in the future in
    //the profile setting page
    private void addUser(String emailS, String nameS, String passwordS) {

        FirebaseDatabase mref = FirebaseDatabase.getInstance();

        DatabaseReference db = mref
                .getReference()
                .child("Users");

        RatingModel rating = new RatingModel(
                nameS,
                img,
                "5.0",
                "0"
        );

        String newKey = db.push().getKey();
        db.child(newKey).child("email").setValue(emailS);
        db.child(newKey).child("name").setValue(nameS);
        db.child(newKey).child("password").setValue(passwordS);
        db.child(newKey).child("Rating").setValue(rating);
        db.child(newKey).child("avatar").setValue(img);
        //db.child(newKey).child("user_rating").setValue("5.0");
        //db.child(newKey).child("rating_amount").setValue("0");
        //db.child(newKey).child("avatar").setValue(img);
    }
}
