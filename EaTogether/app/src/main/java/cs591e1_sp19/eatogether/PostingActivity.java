package cs591e1_sp19.eatogether;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostingActivity extends AppCompatActivity {
    DatabaseReference ref;

    Button new_post;

    TextView res_name;

    String time1;
    String time2;
    String note;

    String latitude, longitude, res_rating, res_type;

    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    String res_img;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference("Posts");

        new_post = findViewById(R.id.btn_new_post);
        res_name = findViewById(R.id.res_name);

        res_name.setText(getIntent().getStringExtra("resName"));
        res_img = getIntent().getStringExtra("resImg");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");
        year = getIntent().getIntExtra("year", 2019);
        month = getIntent().getIntExtra("month", 4);
        day = getIntent().getIntExtra("day", 30);
        res_rating = getIntent().getStringExtra("rating");
        res_type = getIntent().getStringExtra("type");

        final String res_id = getIntent().getStringExtra("resID");


        // Get time picker object.
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                PostingActivity.this.hour = hour;
                PostingActivity.this.minute = minute;
                time1 = String.valueOf(hour) + ":" + String.valueOf(minute);
                time2 = String.valueOf(hour + 3) + ":" + String.valueOf(minute);
            }
        });

        //we would first first create the post model here, so it's better for out post adapter and firebase uploading
        new_post.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DatabaseReference ref_id = ref.child(res_id);
                DatabaseReference ref_post = ref_id.push();
                ref_post.setValue(new PostModel(AppState.userID,
                        AppState.userName,
                        AppState.userAvatar,
                        String.valueOf(day),
                        String.valueOf(month + 1),
                        String.valueOf(year),
                        time1,
                        time2,
                        note,
                        res_id,
                        res_name.getText().toString(),
                        ref_post.getKey(),
                        res_img,
                        latitude,
                        longitude,
                        res_rating,
                        res_type));
                DatabaseReference ref_user = database.getReference("Users").child(AppState.userID).child("Posts");
                ref_user.child(ref_post.getKey()).setValue(res_id);
                // Intent i = new Intent(getApplicationContext(),MapsActivity.class);
                // startActivity(i);

                Intent intent = new Intent(PostingActivity.this, MyPosts.class);

                startActivity(intent);
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

}
