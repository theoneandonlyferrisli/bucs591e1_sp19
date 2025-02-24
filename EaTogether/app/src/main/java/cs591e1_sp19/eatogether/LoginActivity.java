package cs591e1_sp19.eatogether;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cs591e1_sp19.eatogether.AppState.current_lati;
import static cs591e1_sp19.eatogether.AppState.current_longi;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    EditText loginEmail,loginPassword;
    Button loginButton,registerButton,newPassButton;

    // we want to ensure the code is the same so we can check the result is for us to authentication
    private static final int RC_SIGN_IN = 9001;
    private SignInButton signInButton;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient mGoogleApiClient;

    //Yelp Class
    YelpFusionApiFactory apiFactory;
    YelpFusionApi yelpFusionApi;

    String apiKey = BuildConfig.YelpApiKey;
    Map<String, String> params;


    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;

    //we'll first sign one picture for users, since we don't have a local database, so for speed consideration, we just hard-coding here,
    //of our future job is to build a local cache so such thing can be much faster
    final private String img = "https://firebasestorage.googleapis.com/v0/b/eatogether-cs591.appspot.com/o/profile_img-01.png?alt=media&token=ef833632-1a12-47cf-aecf-a4504abe9c02";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        loginEmail = (EditText) findViewById(R.id.userEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);


        firebaseAuth = FirebaseAuth.getInstance();

        //Yelp set up
        try {
            apiFactory  = new YelpFusionApiFactory();
            yelpFusionApi = apiFactory.createAPI(apiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(LoginActivity.this,this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
//                .build();

        mGoogleApiClient = GoogleSignIn.getClient(this, gso);

        //the button for Google Login
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailS = loginEmail.getText().toString();
                final String passwordS = loginPassword.getText().toString();


                // we want to first check if the format is right
                if(TextUtils.isEmpty(emailS)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(passwordS)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                }

                if(passwordS.length()<6){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
                }

                logIn(emailS, passwordS);
            }
        });
    }

    private void signIn() {
        Intent signIntent = mGoogleApiClient.getSignInIntent();

        // startActivityForResult will start another activity from this activity and expect result to return
        startActivityForResult(signIntent,RC_SIGN_IN);
    }

    //onActivityResult is where we get the result returned from newly created class
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
                if (acct != null) {
                    String personName = acct.getDisplayName();
                    String personEmail = acct.getEmail();
                    String personId = acct.getId();
                    checkSignLogin(personEmail, personId, personName);
                } else {
                    Toast.makeText(getApplicationContext(), "Try Agian!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void logIn(final String email, final String password) {
        FirebaseDatabase mref = FirebaseDatabase.getInstance();

        final DatabaseReference db = mref
                .getReference()
                .child("Users");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    if(userSnapShot.child("email").getValue().equals(email) && userSnapShot.child("password").getValue().equals(password)) {
                        Toast.makeText(getApplicationContext(), "Login Successfully", Toast.LENGTH_SHORT).show();

                        AppState.isLoggedIn = true;
                        AppState.userID = userSnapShot.getKey();

                        RatingModel rating = userSnapShot.child("Rating").getValue(RatingModel.class);
                        AppState.userName = rating.username;
                        AppState.userRating = Float.parseFloat(rating.rating);
                        AppState.ratingAmount = Integer.parseInt(rating.rating_amount);
                        AppState.userAvatar = rating.useravatar;

                        /*if (userSnapShot.hasChild("Post")) {
                            AppState.userPost = userSnapShot.child("Post").child("PostID").getValue().toString();
                            Log.v("test_login", AppState.userPost);
                        } else {
                            AppState.userPost = null;
                        }*/


                    }
                }

                if(!AppState.isLoggedIn) {
                    Toast.makeText(getApplicationContext(), "login Faild, try again", Toast.LENGTH_SHORT).show();
                } else {
                    // login successfully, we can access the newarby restaurants
                    params = new HashMap<>();

                    if(getLocation()){

                        Log.v("test_logout", current_lati);
                        Log.v("test_logout", current_longi);
                        params.put("latitude", current_lati);
                        params.put("longitude", current_longi);
                        params.put("radius", "3000");

                        Call<SearchResponse> call = yelpFusionApi.getBusinessSearch(params);
                        call.enqueue(callback);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //we write the getLocation fnuction at this place because
    private boolean getLocation() {
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                current_lati = String.valueOf(latti);
                current_longi = String.valueOf(longi);
                return true;

            } else if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                current_lati = String.valueOf(latti);
                current_longi = String.valueOf(longi);
                return true;

            } else if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                current_lati = String.valueOf(latti);
                current_longi = String.valueOf(longi);
                return true;

            } else {

                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();
                return false;

            }
        }

        return false;
    }

    Callback<SearchResponse> callback = new Callback<SearchResponse>() {
        @Override
        public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
            SearchResponse searchResponse = response.body();
            // Update UI text with the searchResponse.
            final ArrayList<Business> businesses = searchResponse.getBusinesses();

            FirebaseDatabase mref = FirebaseDatabase.getInstance();

            DatabaseReference db = mref
                    .getReference()
                    .child("Users")
                    .child(AppState.userID)
                    .child("Nearby");

            db.removeValue();

            for(Business bu : businesses) {
                DatabaseReference restaurant = db.child(bu.getId());
                restaurant.setValue(new MapModel(bu.getName(),
                        bu.getPrice(),
                        bu.getRating(),
                        bu.getCategories(),
                        bu.getCoordinates()));
            }

            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            // Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            // Intent intent = new Intent(getApplicationContext(), OnGoingActivity.class);
            startActivity(intent);
        }
        @Override
        public void onFailure(Call<SearchResponse> call, Throwable t) {
            // HTTP error happened, do something to handle it.
        }
    };


    private void authWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Auth Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkSignLogin(final String personEmail, final String personId, final String personName) {
        FirebaseDatabase mref = FirebaseDatabase.getInstance();
        final boolean[] exist = new boolean[1];
        exist[0] = false;

        DatabaseReference db = mref.getReference().child("Users");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    if(userSnapShot.child("email").getValue().toString().equals(personEmail)) {
                        exist[0] = true;
                        break;
                    }
                }

                if(exist[0]) {
                    logIn(personEmail, personId);
                } else {
                    addUser(personEmail, personName, personId);
                    logIn(personEmail, personId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onBackPressed() {
// super.onBackPressed();
// Not calling **super**, disables back button in current screen.
    }
}
