package in.mittt.tt18.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.mittt.tt18.R;
import in.mittt.tt18.adapters.CategoryEventsAdapter;
import in.mittt.tt18.models.events.EventDetailsModel;
import in.mittt.tt18.models.events.EventModel;
import in.mittt.tt18.models.events.ScheduleModel;
import in.mittt.tt18.models.registration.EventRegistrationResponse;
import in.mittt.tt18.network.RegistrationClient;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity implements CategoryEventsAdapter.EventLongPressListener{

    private String catName;
    private String cat_id;
    //    private String catDesc;
    private Realm mDatabase;
    private TextView noEventsDay1;
    private TextView noEventsDay2;
    private TextView noEventsDay3;
    private TextView noEventsDay4;
    //    private TextView noEventsPreRevels;
    private TextView catNameTextView;
    private TextView catDescTextView;
    private List<ScheduleModel> scheduleResults;
    private String TAG = "CategoryActivity";

    public void onItemLongPress(EventModel event){
        registerForEvent(event.getEventId());
    }
    private void registerForEvent(String eventID){
        Log.d(TAG, "registerForEvent: called");
        final ProgressDialog dialog = new ProgressDialog(getBaseContext());
        dialog.setMessage("Trying to register you for event... please wait!");
        dialog.setCancelable(false);
        dialog.show();
        RequestBody body =  RequestBody.create(MediaType.parse("text/plain"), "eventid="+eventID);
        Call<EventRegistrationResponse> call = RegistrationClient.getRegistrationInterface(getBaseContext()).eventReg(RegistrationClient.generateCookie(getBaseContext()),body);
        call.enqueue(new Callback<EventRegistrationResponse>() {
            @Override
            public void onResponse(Call<EventRegistrationResponse> call, Response<EventRegistrationResponse> response) {
                dialog.cancel();
                if (response != null && response.body() != null){
                    try {
                        showAlert(response.body().getMessage());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    showAlert("Error! Please try again.");
                }
            }
            @Override
            public void onFailure(Call<EventRegistrationResponse> call, Throwable t) {
                dialog.cancel();
                showAlert("Error connecting to server! Please try again.");
            }
        });

    }
    public void showAlert(String message) {
        new AlertDialog.Builder(getBaseContext()).setIcon(R.drawable.ic_info).setTitle("Info").setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setCancelable(true).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        catName = getIntent().getStringExtra("catName");
        cat_id = getIntent().getStringExtra("catId");
//        catDesc = getIntent().getStringExtra("catDesc");
        Log.d(TAG, "catName = " + catName + "\ncat_id = " + cat_id);
        if (catName == null) catName = "";
        if (cat_id == null) cat_id = "";
//        if (catDesc == null) catDesc = "";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AppBarLayout appBar = findViewById(R.id.app_bar);
                appBar.setElevation((4 * getResources().getDisplayMetrics().density + 0.5f));
                Toolbar toolbar = findViewById(R.id.category_toolbar);
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(catName);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setElevation((4 * getResources().getDisplayMetrics().density + 0.5f));
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        mDatabase = Realm.getDefaultInstance();
        displayEvents();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

//            case R.id.about_category:
//                View view = View.inflate(this, R.layout.dialog_about_category, null);
//                final Dialog dialog = new Dialog(this);
//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                dialog.setContentView(view);
//                catNameTextView = view.findViewById(R.id.category_about_name);
//                catDescTextView = view.findViewById(R.id.category_about_description);
//                catNameTextView.setText(catName);
////                catDescTextView.setText(catDesc);
//
//                dialog.show();
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayEvents() {

//        List<EventModel> preRevelsList = new ArrayList<>();
        List<EventModel> day1List=new ArrayList<>();
        List<EventModel> day2List=new ArrayList<>();
        List<EventModel> day3List=new ArrayList<>();
        List<EventModel> day4List=new ArrayList<>();

//        noEventsPreRevels = findViewById(R.id.cat_pre_revels_no_events);
        noEventsDay1 = findViewById(R.id.cat_day_1_no_events);
        noEventsDay2 = findViewById(R.id.cat_day_2_no_events);
        noEventsDay3 = findViewById(R.id.cat_day_3_no_events);
        noEventsDay4 = findViewById(R.id.cat_day_4_no_events);

        if (mDatabase == null)
            return;

        RealmResults<ScheduleModel> scheduleResultsRealm = mDatabase.where(ScheduleModel.class).equalTo("catId", cat_id).findAll().sort("startTime");
        scheduleResults = mDatabase.copyFromRealm(scheduleResultsRealm);
        for (ScheduleModel schedule : scheduleResults) {
//            if (schedule.getIsRevels().contains("0")) {
//                Log.d(TAG, "displayEvents: PreRevels");
//                EventDetailsModel eventDetails = mDatabase.where(EventDetailsModel.class).equalTo("eventID", schedule.getEventID()).findFirst();
//                EventModel event = new EventModel(eventDetails, schedule);
//                preRevelsList.add(event);
//            } else {

            Log.d(TAG, schedule.toString());
            EventDetailsModel eventDetails = mDatabase.where(EventDetailsModel.class).equalTo("eventId", schedule.getEventId()).findFirst();
                EventModel event = new EventModel(eventDetails, schedule);
                switch (event.getDay()) {
                    case "1":
                        day1List.add(event);
                        break;
                    case "2":
                        day2List.add(event);
                        break;
                    case "3":
                        day3List.add(event);
                        break;
                    case "4":
                        day4List.add(event);
                        break;
                }
//            }
        }
//        preRevelsEventSort(preRevelsList);
        eventSort(day1List);
        eventSort(day2List);
        eventSort(day3List);
        eventSort(day4List);

//        RecyclerView recyclerViewDayPreRevels = findViewById(R.id.category_pre_revels_recycler_view);
//        if (preRevelsList.isEmpty()) {
//            noEventsPreRevels.setVisibility(View.VISIBLE);
//            recyclerViewDayPreRevels.setVisibility(View.GONE);
//        } else {
//            recyclerViewDayPreRevels.setAdapter(new CategoryEventsAdapter(preRevelsList, this, getBaseContext(), false));
//            recyclerViewDayPreRevels.setNestedScrollingEnabled(false);
//            recyclerViewDayPreRevels.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        }

        RecyclerView recyclerViewDay1 = findViewById(R.id.category_day_1_recycler_view);
        if(day1List.isEmpty()){
            noEventsDay1.setVisibility(View.VISIBLE);
            recyclerViewDay1.setVisibility(View.GONE);
        }
        else{
            recyclerViewDay1.setAdapter(new CategoryEventsAdapter(day1List, this, getBaseContext(), true, this));
            recyclerViewDay1.setNestedScrollingEnabled(false);
            recyclerViewDay1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        RecyclerView recyclerViewDay2 = findViewById(R.id.category_day_2_recycler_view);
        if(day2List.isEmpty()){
            noEventsDay2.setVisibility(View.VISIBLE);
            recyclerViewDay2.setVisibility(View.GONE);
        }
        else{
            recyclerViewDay2.setAdapter(new CategoryEventsAdapter(day2List, this, getBaseContext(), true, this));
            recyclerViewDay2.setNestedScrollingEnabled(false);
            recyclerViewDay2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        RecyclerView recyclerViewDay3 = findViewById(R.id.category_day_3_recycler_view);
        if(day3List.isEmpty()){
            noEventsDay3.setVisibility(View.VISIBLE);
            recyclerViewDay3.setVisibility(View.GONE);
        }
        else {
            recyclerViewDay3.setAdapter(new CategoryEventsAdapter(day3List, this, getBaseContext(), true, this));
            recyclerViewDay3.setNestedScrollingEnabled(false);
            recyclerViewDay3.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        RecyclerView recyclerViewDay4 = findViewById(R.id.category_day_4_recycler_view);
        if(day4List.isEmpty()){
            noEventsDay4.setVisibility(View.VISIBLE);
            recyclerViewDay4.setVisibility(View.GONE);
        }
        else {
            recyclerViewDay4.setAdapter(new CategoryEventsAdapter(day4List, this, getBaseContext(), true, this));
            recyclerViewDay4.setNestedScrollingEnabled(false);
            recyclerViewDay4.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }
    }

//    private void preRevelsEventSort(List<EventModel> eventsList) {
//
//        Collections.sort(eventsList, new Comparator<EventModel>() {
//            @Override
//            public int compare(EventModel o1, EventModel o2) {
//                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.US);
//
//                try {
//
//                    Date d1 = sdf.parse(o1.getDay());
//                    Date d2 = sdf.parse(o2.getDay());
//
//                    Calendar c1 = Calendar.getInstance();
//                    c1.setTime(d1);
//                    Calendar c2 = Calendar.getInstance();
//                    c2.setTime(d2);
//
//                    long diff = c1.getTimeInMillis() - c2.getTimeInMillis();
//                    if (diff > 0) return 1;
//                    else if (diff < 0) return -1;
//                    else {
//
//                        Date d3 = sdf.parse(o1.getStartTime());
//                        Date d4 = sdf.parse(o2.getStartTime());
//
//                        Calendar c3 = Calendar.getInstance();
//                        c1.setTime(d3);
//                        Calendar c4 = Calendar.getInstance();
//                        c2.setTime(d4);
//
//                        long diff2 = c3.getTimeInMillis() - c4.getTimeInMillis();
//
//                        if (diff2 > 0) return 1;
//                        else if (diff2 < 0) return -1;
//                        else {
//                            int catDiff = o1.getCatName().compareTo(o2.getCatName());
//
//                            if (catDiff > 0) return 1;
//                            else if (catDiff < 0) return -1;
//                            else {
//                                int eventDiff = o1.getEventName().compareTo(o2.getEventName());
//
//                                if (eventDiff > 0) return 1;
//                                else if (eventDiff < 0) return -1;
//                                else return 0;
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return 0;
//            }
//        });
//
//    }

    private void eventSort(List<EventModel> eventsList){
        Collections.sort(eventsList, new Comparator<EventModel>() {
            @Override
            public int compare(EventModel o1, EventModel o2) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.US);

                try {
                    Date d1 = sdf.parse(o1.getStartTime());
                    Date d2 = sdf.parse(o2.getStartTime());

                    Calendar c1 = Calendar.getInstance();
                    c1.setTime(d1);
                    Calendar c2 = Calendar.getInstance();
                    c2.setTime(d2);

                    long diff = c1.getTimeInMillis() - c2.getTimeInMillis();

                    if (diff>0) return 1;
                    else if (diff<0) return -1;
                    else{
                        int catDiff = o1.getCatName().compareTo(o2.getCatName());

                        if (catDiff>0) return 1;
                        else if (catDiff<0) return -1;
                        else {
                            int eventDiff = o1.getEventName().compareTo(o2.getEventName());

                            if (eventDiff>0) return 1;
                            else if (eventDiff<0) return -1;
                            else return 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mDatabase.close();
        mDatabase =null;
    }
}
