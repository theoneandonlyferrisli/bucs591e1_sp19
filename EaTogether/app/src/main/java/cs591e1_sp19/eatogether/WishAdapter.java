package cs591e1_sp19.eatogether;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class WishAdapter extends RecyclerView.Adapter<WishAdapter.MyViewHolder> {

    Context context;
    ArrayList<Restaurant> restaurants;
    private String rest_id;
    private String target_rest;


    public WishAdapter(Context c, ArrayList<Restaurant> r){
        context = c;
        restaurants = r;
    }

    @NonNull
    @Override
    public WishAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.listview_row,viewGroup, false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) { //retrieve data and show it in the view
        holder.tvName.setText(restaurants.get(position).getName());
        holder.res_rating.setRating(Float.parseFloat(restaurants.get(position).getRating()));
        holder.types.setText(restaurants.get(position).getType());
        Picasso.get().load(restaurants.get(position).getImageUrl()).into(holder.imgRestaurant);

    }

    @Override
    public int getItemCount() {   //get the number of restaurants in wishlist
        return restaurants.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView tvName, types;
        public ImageView imgRestaurant;
        public RatingBar res_rating;
        //public Button btnDetail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.business_name);
            imgRestaurant = itemView.findViewById(R.id.business_img);
            types = itemView.findViewById(R.id.business_type);
            res_rating = itemView.findViewById(R.id.business_rating);
            //btnDetail = itemView.findViewById(R.id.btnDetail);


            /*btnDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Go through the database and look for the name of current restaurant
                    //get restaurant ID, and call the yelp api using the id to get the restaurant detail page

                    FirebaseDatabase.getInstance().getReference().child("Users").child(AppState.userID)
                            .child("Restaurants")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        String Res_Name = String.valueOf(snapshot.child("Name").getValue());
                                        target_rest = tvName.getText().toString();

                                        if (target_rest.equals(Res_Name)){

                                            rest_id = snapshot.getKey();
                                            System.out.println("rest_id");
                                            System.out.println(rest_id);
                                            Intent intent = new Intent(context, RestaurantPost.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("rest_id", rest_id);
                                            context.startActivity(intent);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                }
            });*/

            //Go through the database and look for the name of current restaurant
            //get restaurant ID, and call the yelp api using the id to get the restaurant detail page
            imgRestaurant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FirebaseDatabase.getInstance().getReference().child("Users").child(AppState.userID)
                            .child("Restaurants")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        String Res_Name = String.valueOf(snapshot.child("Name").getValue());
                                        target_rest = tvName.getText().toString();

                                        if (target_rest.equals(Res_Name)){

                                            rest_id = snapshot.getKey();
                                            System.out.println("rest_id");
                                            System.out.println(rest_id);
                                            Intent intent = new Intent(context, RestaurantPost.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("rest_id", rest_id);
                                            context.startActivity(intent);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                }
            });



        }
    }

}
