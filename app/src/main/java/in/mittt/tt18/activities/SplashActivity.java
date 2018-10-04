package in.mittt.tt18.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import in.mittt.tt18.R;
import in.mittt.tt18.models.categories.CategoriesListModel;
import in.mittt.tt18.models.categories.CategoryModel;
import in.mittt.tt18.models.events.EventDetailsModel;
import in.mittt.tt18.models.events.EventsListModel;
import in.mittt.tt18.models.events.ScheduleListModel;
import in.mittt.tt18.models.events.ScheduleModel;
import in.mittt.tt18.models.results.ResultModel;
import in.mittt.tt18.models.results.ResultsListModel;
import in.mittt.tt18.network.APIClient;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    boolean isWiFi;
    boolean isConnected;
    public Runnable test;
    boolean animationEnded = false;
    private Realm mDatabase;
    boolean dataAvailableLocally;
    boolean animationStarted = false;
    int i = 0;
    private RelativeLayout rootLayout;
    private int counter = 0;
    private int apiCallsRecieved = 0;
    private Context context = this;
    private Handler mHandler = new Handler();
    private boolean eventsDataAvailableLocally = false;
    private boolean schedulesDataAvailableLocally = false;
    private boolean categoriesDataAvailableLocally = false;
    private String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mDatabase = Realm.getDefaultInstance();
        rootLayout = findViewById(R.id.splash_root_layout);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        dataAvailableLocally = sharedPref.getBoolean("dataAvailableLocally", false);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        final ImageView iconLeft = findViewById(R.id.left_icon);
        final ImageView iconRight = findViewById(R.id.right_icon);
        final ImageView text = findViewById(R.id.tt_text);
        final FrameLayout container = findViewById(R.id.frameLayout4);

        iconLeft.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in_first));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                iconRight.setVisibility(View.VISIBLE);
                iconRight.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in_first));
            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                text.setVisibility(View.VISIBLE);
                text.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in_first));
            }
        }, 1000);

        Animation animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in_first);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG, "Splash:  onAnimationStart: Splash animation Started");
                animationStarted = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "Splash:  onAnimationEnd: Splash animation Ended");
                animationEnded = true;
                if (dataAvailableLocally) {
                    Log.d(TAG, "Data avail local");

                    if (isConnected) {
                        Log.d(TAG, "Is connected");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Snackbar.make(rootLayout, "Updating data", Snackbar.LENGTH_SHORT).show();

                                //loadAllFromInternet();
                                Log.d(TAG, "moveForward called");
                                moveForward();
                            }
                        }, 1500);
                    } else {
                        Log.d(TAG, "not connected");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                moveForward();
                            }
                        }, 1500);
                    }

                } else {
                    Log.d(TAG, "Data not avail local");
                    if (!isConnected) {
                        Log.d(TAG, "not connected");
                        final LinearLayout noConnectionLayout = findViewById(R.id.splash_no_connection_layout);
                        Button retry = noConnectionLayout.findViewById(R.id.retry);
                        noConnectionLayout.setVisibility(View.VISIBLE);
                        iconLeft.setVisibility(View.GONE);
                        iconRight.setVisibility(View.GONE);
                        text.setVisibility(View.GONE);
                        container.setVisibility(View.GONE);
                        retry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ConnectivityManager cmTemp = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo activeNetworkTemp = cmTemp != null ? cmTemp.getActiveNetworkInfo() : null;
                                boolean isConnectedTemp = activeNetworkTemp != null && activeNetworkTemp.isConnectedOrConnecting();

                                if (isConnectedTemp) {
                                    noConnectionLayout.setVisibility(View.GONE);
                                    iconLeft.setVisibility(View.VISIBLE);
                                    iconRight.setVisibility(View.VISIBLE);
                                    text.setVisibility(View.VISIBLE);
                                    container.setVisibility(View.VISIBLE);
                                    Snackbar.make(rootLayout, "Loading data... takes a couple of seconds.", Snackbar.LENGTH_SHORT).show();
                                    try {
                                        loadAllFromInternet();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Snackbar.make(rootLayout, "Check connection!", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, " connected");
                        Snackbar.make(rootLayout, "Loading data... takes a couple of seconds.", Snackbar.LENGTH_SHORT).show();
                        try {
                            loadAllFromInternet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        text.startAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResumeSplash:  Called");
        if (animationStarted && !animationEnded) {
            Log.d(TAG, "onResumeSplash: freeze Helper called");
            freezeSplashHelper();
        }
    }

    private void freezeSplashHelper() {
        final ImageView iconLeft = findViewById(R.id.left_icon);
        final ImageView iconRight = findViewById(R.id.right_icon);
        final ImageView text = findViewById(R.id.tt_text);
        final FrameLayout container = findViewById(R.id.frameLayout4);
        if (dataAvailableLocally) {
            Log.d(TAG, "Data avail local");

            if (isConnected) {
                Log.d(TAG, "Is connected");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Snackbar.make(rootLayout, "Updating data", Snackbar.LENGTH_SHORT).show();

                        //loadAllFromInternet();
                        moveForward();
                    }
                }, 1500);
            } else {
                Log.d(TAG, "not connected");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveForward();
                    }
                }, 1500);
            }

        } else {
            Log.d(TAG, "Data not avail local");
            if (!isConnected) {
                Log.d(TAG, "not connected");
                final LinearLayout noConnectionLayout = findViewById(R.id.splash_no_connection_layout);
                Button retry = noConnectionLayout.findViewById(R.id.retry);
                noConnectionLayout.setVisibility(View.VISIBLE);
                iconLeft.setVisibility(View.GONE);
                iconRight.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                retry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ConnectivityManager cmTemp = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetworkTemp = cmTemp != null ? cmTemp.getActiveNetworkInfo() : null;
                        boolean isConnectedTemp = activeNetworkTemp != null && activeNetworkTemp.isConnectedOrConnecting();

                        if (isConnectedTemp) {
                            noConnectionLayout.setVisibility(View.GONE);
                            iconLeft.setVisibility(View.VISIBLE);
                            iconRight.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                            container.setVisibility(View.VISIBLE);
                            Snackbar.make(rootLayout, "Loading data... takes a couple of seconds.", Snackbar.LENGTH_SHORT).show();
                            try {
                                loadAllFromInternet();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Snackbar.make(rootLayout, "Check connection!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Log.d(TAG, " connected");
                Snackbar.make(rootLayout, "Loading data... takes a couple of seconds.", Snackbar.LENGTH_SHORT).show();
                try {
                    loadAllFromInternet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadAllFromInternet() {
        loadEventsFromInternet();
        loadSchedulesFromInternet();
        loadCategoriesFromInternet();

        test = new Runnable() {
            @Override
            public void run() {
                if (eventsDataAvailableLocally && schedulesDataAvailableLocally && categoriesDataAvailableLocally) {
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("dataAvailableLocally", true);
                    editor.apply();
                    if (!dataAvailableLocally) {
                        moveForward();
                    }
                }
                if (!(eventsDataAvailableLocally && schedulesDataAvailableLocally && categoriesDataAvailableLocally)) {
                    counter++;
                    if (apiCallsRecieved == 3) {
                        if (i == 0) {
                            i = counter;
                        }
                        Snackbar.make(rootLayout, "Error in retrieving data. Some data may be outdated", Snackbar.LENGTH_SHORT).show();
                        if (counter - i == 1) {
                            moveForward();
                        }
                    }
                    if (counter == 10 && !dataAvailableLocally) {
                        if (eventsDataAvailableLocally || schedulesDataAvailableLocally || categoriesDataAvailableLocally) {
                            Snackbar.make(rootLayout, "Possible slow internet connection", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(rootLayout, "Server may be down. Please try again later", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    mHandler.postDelayed(test, 1500);
                }
            }
        };
        mHandler.postDelayed(test, 1500);
    }

    private void moveForward() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadCategoriesFromInternet() {
        try {
            Call<CategoriesListModel> categoriesCall = APIClient.getAPIInterface().getCategoriesList();
            categoriesCall.enqueue(new Callback<CategoriesListModel>() {
                @Override
                public void onResponse(@NonNull Call<CategoriesListModel> call,
                                       @NonNull Response<CategoriesListModel> response) {
                    if (response.isSuccessful() && response.body() != null && mDatabase != null) {
                        apiCallsRecieved++;
                        mDatabase.beginTransaction();
                        mDatabase.where(CategoryModel.class).findAll().deleteAllFromRealm();
                        mDatabase.copyToRealmOrUpdate(response.body().getCategoriesList());
                        mDatabase.commitTransaction();
                        categoriesDataAvailableLocally = true;
                        Log.d("TAG", "Categories");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CategoriesListModel> call, @NonNull Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadSchedulesFromInternet() {
        try {
            Call<ScheduleListModel> schedulesCall = APIClient.getAPIInterface().getScheduleList();
            schedulesCall.enqueue(new Callback<ScheduleListModel>() {
                @Override
                public void onResponse(@NonNull Call<ScheduleListModel> call, @NonNull Response<ScheduleListModel> response) {
                    if (response.isSuccessful() && response.body() != null && mDatabase != null) {
                        apiCallsRecieved++;
                        mDatabase.beginTransaction();
                        mDatabase.where(ScheduleModel.class).findAll().deleteAllFromRealm();
                        mDatabase.copyToRealm(response.body().getData());
                        mDatabase.commitTransaction();
                        schedulesDataAvailableLocally = true;
                        Log.d(TAG, "Schedules");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ScheduleListModel> call, @NonNull Throwable t) {
                    apiCallsRecieved++;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadEventsFromInternet() {
        try {
            Call<EventsListModel> eventsCall = APIClient.getAPIInterface().getEventsList();
            eventsCall.enqueue(new Callback<EventsListModel>() {
                @Override
                public void onResponse(@NonNull Call<EventsListModel> call, @NonNull Response<EventsListModel> response) {
                    if (response.isSuccessful() && response.body() != null && mDatabase != null) {
                        apiCallsRecieved++;
                        mDatabase.beginTransaction();
                        mDatabase.where(EventDetailsModel.class).findAll().deleteAllFromRealm();
                        mDatabase.copyToRealmOrUpdate(response.body().getEvents());
                        mDatabase.commitTransaction();
                        eventsDataAvailableLocally = true;
                        Log.d("TAG", "Events");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<EventsListModel> call, @NonNull Throwable t) {
                    apiCallsRecieved++;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }
}
