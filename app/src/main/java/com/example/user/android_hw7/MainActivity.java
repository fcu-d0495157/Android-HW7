package com.example.user.android_hw7;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private HotelArrayAdapter adapter = null;

    private  static final int LIST_HOTELS = 1;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case LIST_HOTELS: {
                    List<hotel> hotels = (List<hotel>)msg.obj;
                    refreshHotelList(hotels);
                    break;
                }
            }
        }
    };

    private void refreshHotelList(List<hotel> hotels){
        adapter.clear();
        adapter.addAll(hotels);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvHotels = (ListView)findViewById(R.id.listview_hotel);

        HotelArrayAdapter adapter = new HotelArrayAdapter(this, new ArrayList<hotel>());
        lvHotels.setAdapter(adapter);

        getHotelFromFirebase();
    }



    private void getHotelFromFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new FirebaseThread(dataSnapshot).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("Hotel" , databaseError.getMessage());
            }
        });
    }

    class FirebaseThread extends Thread{

        private  DataSnapshot dataSnapshot;

        public  FirebaseThread(DataSnapshot dataSnapshot){
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public void run(){
            List<hotel> IsHotels = new ArrayList<>();
            for(DataSnapshot ds : dataSnapshot.getChildren()){
                DataSnapshot dsSName = ds.child("Name");
                DataSnapshot dsSAdd = ds.child("Add");
                DataSnapshot dsSTel = ds.child("Tel");

                String Name = (String)dsSName.getValue();
                String Add = (String)dsSAdd.getValue();
                String Tel = (String)dsSTel.getValue();

                hotel ahotel = new hotel();
                ahotel.setName(Name);
                ahotel.setAdd(Add);
                ahotel.setTel(Tel);
                IsHotels.add(ahotel);

                Message msg = new Message();
                msg.what = LIST_HOTELS;
                msg.obj = IsHotels;
                handler.sendMessage(msg);
            }
        }
    }

    class HotelArrayAdapter extends ArrayAdapter<hotel> {
        Context context;

        public HotelArrayAdapter(Context context, List<hotel> items){
            super(context,0,items);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout itemlayout = null;
            if (convertView == null) {
                itemlayout = (LinearLayout) inflater.inflate(R.layout.hotel_item, null);
            } else {
                itemlayout = (LinearLayout) convertView;
            }
            hotel item = (hotel) getItem(position);
            TextView tv_name = (TextView)itemlayout.findViewById(R.id.tv_name);
            tv_name.setText(item.getName());

            TextView tv_add = (TextView)itemlayout.findViewById(R.id.tv_add);
            tv_add.setText(item.getAdd());

            TextView tv_tel = (TextView)itemlayout.findViewById(R.id.tv_tel);
            tv_tel.setText(item.getTel());
            return itemlayout;
        }

    }
}
