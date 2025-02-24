package cs591e1_sp19.eatogether;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatModel {

    public String creator_id, creator_name, creator_avatar, post_id, res_id, res_name, time1, time2;
    public HashMap<String, String> guests;
    public String res_latitude, res_longitude;
    public String status;
    public String date, month, year;


    public ChatModel(String creator_id, String creator_name,
                     String creator_avatar, HashMap<String, String> guests,
                     String post_id, String res_id, String res_name,
                     String latitude, String longitude,
                     String time1, String time2, String date,
                     String month, String year, String status){

        this.creator_id = creator_id;
        this.creator_name = creator_name;
        this.creator_avatar = creator_avatar;
        this.guests = guests;
        this.post_id = post_id;
        this.res_id = res_id;
        this.res_name = res_name;
        this.res_latitude = latitude;
        this.res_longitude = longitude;
        this.time1 = time1;
        this.time2 = time2;
        this.date = date;
        this.month = month;
        this.year = year;
        this.status = status;
    }

    public ChatModel(){

    }

}
