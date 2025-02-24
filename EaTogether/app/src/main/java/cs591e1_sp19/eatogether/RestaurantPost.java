package cs591e1_sp19.eatogether;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.Review;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantPost extends AppCompatActivity {

    YelpFusionApiFactory apiFactory;
    YelpFusionApi yelpFusionApi;
    String apiKey = BuildConfig.YelpApiKey;

    RecyclerView posts_view;
    PostAdapter posts_adapter;

    ArrayList<PostModel> posts_lst;
    TextView res_name,res_review,res_type;
    RatingBar res_rating;
    //ImageView res_img;
    ArrayList<ImageView> imgs;
    ImageView img1, img2, img3;
    Button post;
    MaterialFavoriteButton favorite;

    private RelativeLayout rll_notice;
    private ImageView notice;
    private int count=0;

    String types = "";

    //the blank space on the bottom is for showing existing posts ones the back end is finished.
    //the post button does not have any functionality for now, since we do not have a back end yet.
    //click on the TextView review to see the reviews.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_post);

        Firebase.setAndroidContext(this);

        favorite=(MaterialFavoriteButton)findViewById(R.id.fav);
        //mRef = new Firebase("https://yelpfusionapi-b5637.firebaseio.com/Restaurants");
        rll_notice= (RelativeLayout) findViewById(R.id.No_Post_Notice);

        try {
            apiFactory  = new YelpFusionApiFactory();
            yelpFusionApi = apiFactory.createAPI(apiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String res_id = getIntent().getStringExtra("rest_id");

        //use the getBusiness function to query the Business API.
        Call<Business> call = yelpFusionApi.getBusiness(res_id);
        call.enqueue(callback);

        //If there is no post in the Restaurant Detail Page
        //Show a image notice dynamically

        FirebaseDatabase.getInstance().getReference().child("Posts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            System.out.println(snapshot.getKey());
                            System.out.println(res_id);
                            if (snapshot.getKey().equals(res_id)){
                                count = 1;
                                break;

                            }

                        }
                        if (count==0){
                            notice = new ImageView(RestaurantPost.this);
                            notice.setLayoutParams(new RelativeLayout.LayoutParams(800, 800));
                            notice.setImageResource(R.drawable.no_post_notice);
                            rll_notice.addView(notice);
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);   //get rid of default behavior.

        // Inflate the menu; this adds items to the action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //open wishlist activity

        int id = item.getItemId();

        if (id == R.id.wish) {
            Intent i = new Intent(this, WishList.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.lst) {
            Intent i = new Intent(this, PostsList.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.map) {
            Intent i = new Intent(this, MapsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);  //if none of the above are true, do the default and return a boolean.
    }

    /*public void setupBadge(TextView countTxt, Integer pendingCount){
        if (pendingCount == 0){
            if (countTxt.getVisibility() != View.GONE){
                countTxt.setVisibility(View.GONE);
            }
        } else {
            countTxt.setText(String.valueOf(Math.min(pendingCount, 99)));
            if (countTxt.getVisibility() != View.VISIBLE){
                    countTxt.setVisibility(View.VISIBLE);
            }
        }
    }*/
    @Override
    public void onBackPressed() {
// super.onBackPressed();
// Not calling **super**, disables back button in current screen.
    }


    Callback<Business> callback = new Callback<Business>() {
        @Override
        public void onResponse(Call<Business> call, Response<Business> response) {

            final Business business = response.body();

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("Posts").child(business.getId());

            res_name = findViewById(R.id.res_name);
            res_rating = findViewById(R.id.res_rating);
            res_review = findViewById(R.id.res_review);
            res_type = findViewById(R.id.res_type);
           // res_img = findViewById(R.id.res_img);
            img1 = findViewById(R.id.img1);
            img2 = findViewById(R.id.img2);
            img3 = findViewById(R.id.img3);
            post = findViewById(R.id.btn_post);
            posts_lst = new ArrayList<>();
            imgs = new ArrayList<>();
            imgs.add(img1);
            imgs.add(img2);
            imgs.add(img3);

            res_name.setText(business.getName());
            res_rating.setRating(Float.parseFloat(Double.toString(business.getRating())));

            for (int i = 0; i < business.getCategories().size(); i++){
                types += business.getCategories().get(i).getTitle();
                if (i != business.getCategories().size() - 1) types += ", ";
            }

            res_type.setText(types);
          //  Picasso.get().load(business.getImageUrl())/*.resize(150,150)*/.into(res_img);
            for (int i = 0 ; i < business.getPhotos().size(); i++){
                Picasso.get().load(business.getPhotos().get(i)).resize(300, 300).into(imgs.get(i));
            }
            //Picasso.get().load(business.getPhotos().get(0)).resize(300,300).into(img1);
            //Picasso.get().load(business.getPhotos().get(1)).resize(300,300).into(img2);
            //Picasso.get().load(business.getPhotos().get(2)).resize(300,300).into(img3);

            Log.v("test_loc", Boolean.toString(AppState.userID == null));

            //Go through the database and compare the current restaurant name with name in wishlist
            //If they are the same, then set favorite button filled.
            FirebaseDatabase.getInstance().getReference().child("Users").child(AppState.userID)
                    .child("Restaurants")
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                                if (business.getId().equals( snapshot.getKey())){
                                    favorite.setFavorite(true);

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        PostModel r = dataSnapshot1.getValue(PostModel.class);
                        if (!AppState.userID.equals(r.user_id)) {
                            Log.v("test_post_app", AppState.userID);
                            Log.v("test_post_db", r.user_id);
                            Log.v("test_post", r.user_name);
                            posts_lst.add(r);
                        }

                    }
                    posts_view = findViewById(R.id.recView_post);
                    posts_view.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    posts_adapter = new PostAdapter(getApplicationContext(), posts_lst);
                    posts_view.setAdapter(posts_adapter);
                    Log.v("test", "setting up adapter");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    Intent i = new Intent(getApplicationContext(), NewPost.class);
                    i.putExtra("resID", business.getId());
                    i.putExtra("resName", business.getName());
                    i.putExtra("resImg", business.getImageUrl());
                    i.putExtra("latitude", Double.toString(business.getCoordinates().getLatitude()));
                    i.putExtra("longitude", Double.toString(business.getCoordinates().getLongitude()));
                    i.putExtra("rating", Double.toString(business.getRating()));
                    i.putExtra("type", types);
                    startActivity(i);

                }
            });

            res_review.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), RestaurantReview.class);
                    i.putExtra("resID", business.getId());
                    startActivity(i);
                }
            });


            //When user clicks the fav button, they name and image of that restaurant will be uploaded to database
            favorite.setOnFavoriteChangeListener(  // add restaurant if to database if user like it
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {

                            DatabaseReference mRef = database.getReference("Users")
                                    .child(AppState.userID)
                                    .child("Restaurants");
                            //DatabaseReference mRef = database.getReference("Restaurants"); BEFORE USERS WERE ADDED TO DB
                            DatabaseReference noRef = mRef.child(business.getId());
                            DatabaseReference nameRef = noRef.child("Name");
                            DatabaseReference imageRef = noRef.child("imageUrl");
                            DatabaseReference ratingRef = noRef.child("Rating");
                            DatabaseReference typeRef = noRef.child("Type");

                            if (favorite == true){

                                nameRef.setValue(business.getName());

                                imageRef.setValue(business.getImageUrl());

                                ratingRef.setValue(Double.toString(business.getRating()));

                                typeRef.setValue(types);

                            }
                            else {
                                nameRef.setValue(null);   //unclick the fav button
                                imageRef.setValue(null);

                            }
                        }
                    });

        }

        @Override
        public void onFailure(Call<Business> call, Throwable t) {
            Log.v("error", "failed api call");
        }
    };
}


