package in.mittt.tt18.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import in.mittt.tt18.R;
import in.mittt.tt18.activities.FavouritesActivity;
import in.mittt.tt18.activities.LoginActivity;
import in.mittt.tt18.activities.MainActivity;
import in.mittt.tt18.activities.ProfileActivity;
import in.mittt.tt18.adapters.EventsAdapter;
import in.mittt.tt18.adapters.HomeAdapter;
import in.mittt.tt18.adapters.HomeCategoriesAdapter;
import in.mittt.tt18.adapters.HomeEventsAdapter;
import in.mittt.tt18.adapters.HomeResultsAdapter;
import in.mittt.tt18.models.categories.CategoryModel;
import in.mittt.tt18.models.events.ScheduleModel;
import in.mittt.tt18.models.favourites.FavouritesModel;
import in.mittt.tt18.models.instagram.InstagramFeed;
import in.mittt.tt18.models.registration.EventRegistrationResponse;
import in.mittt.tt18.models.results.EventResultModel;
import in.mittt.tt18.models.results.ResultModel;
import in.mittt.tt18.models.results.ResultsListModel;
import in.mittt.tt18.network.APIClient;
import in.mittt.tt18.network.InstaFeedAPIClient;
import in.mittt.tt18.network.RegistrationClient;
import in.mittt.tt18.utilities.NetworkUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private MainActivity mMainActivity;
    SwipeRefreshLayout swipeRefreshLayout;
    View v;
    String TAG = "HomeFragment";
    private InstagramFeed feed;
    private HomeAdapter instaAdapter;
    private HomeResultsAdapter resultsAdapter;
    private HomeCategoriesAdapter categoriesAdapter;
    private HomeEventsAdapter eventsAdapter;
    private RecyclerView homeRV;
    private RecyclerView resultsRV;
    private RecyclerView categoriesRV;
    private RecyclerView eventsRV;
    private TextView resultsMore;
    private TextView categoriesMore;
    private TextView eventsMore;
    private TextView resultsNone;
    private View blogButton, newsletterButton;
    private FrameLayout homeResultsItem;
    private ProgressBar progressBar;
    private BottomNavigationView navigation;
    private AppBarLayout appBarLayout;
    private TextView instaTextView;
    private boolean initialLoad = true;
    private boolean firstLoad = true;
    private int processes = 0;
    private SliderLayout imageSlider;
    private Realm mDatabase;
    private List<EventResultModel> resultsList = new ArrayList<>();
    private List<CategoryModel> categoriesList = new ArrayList<>();
    private List<ScheduleModel> eventsList = new ArrayList<>();
    //private FirebaseRemoteConfig firebaseRemoteConfig;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mMainActivity = (MainActivity) getActivity();
            mMainActivity.setTitle(R.string.fest_name);
        }
        setHasOptionsMenu(true);
        mDatabase = Realm.getDefaultInstance();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().findViewById(R.id.toolbar).setElevation((4 * getResources().getDisplayMetrics().density + 0.5f));
                getActivity().findViewById(R.id.app_bar).setElevation((4 * getResources().getDisplayMetrics().density + 0.5f));
                appBarLayout = getActivity().findViewById(R.id.app_bar);
                appBarLayout.setExpanded(true, true);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        showUpdateAlert();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = initViews(inflater, container);
        v = view;
        progressBar = view.findViewById(R.id.insta_progress);
        instaTextView = view.findViewById(R.id.insta_text_view);
        displayInstaFeed();

        blogButton = view.findViewById(R.id.home_blog);
        newsletterButton = view.findViewById(R.id.home_newsletter);
        blogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCCT("https://themitpost.com/techtatva18-liveblog/", getContext());
            }
        });

        newsletterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int today = c.get(Calendar.DATE);
                final int startDate = 3;
                int dayOfFest = today - startDate;
                String URL ;
                switch (dayOfFest){
                    case 0:URL = "https://themitpost.com/techtatva18-newsletter-day-0/";break;
                    case 1:URL = "https://themitpost.com/techtatva18-newsletter-day-1/";break;
                    case 2:URL = "https://themitpost.com/techtatva18-newsletter-day-2/";break;
                    case 3:URL = "https://themitpost.com/techtatva18-newsletter-day-3/";break;
                    default:
                        URL = "https://themitpost.com/";
                }
                launchCCT(URL, getContext());
            }
        });

        //Setting up Firebase
        /*try {
            firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .build();
            firebaseRemoteConfig.setConfigSettings(configSettings);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

//        //Checking User's Network Status
//        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (imageSlider == null)
            imageSlider = view.findViewById(R.id.home_image_slider);
//        getImageURLSfromFirebase();
        sliderInit();

        resultsAdapter = new HomeResultsAdapter(resultsList, getActivity());
        resultsRV.setAdapter(resultsAdapter);
        resultsRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        updateResultsList();
        resultsMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MORE Clicked - Take user to Results Fragment
                Log.i(TAG, "onClick: Results more");
                if (mMainActivity != null)
                    mMainActivity.changeFragment(new ResultsFragment());
            }
        });

        //Display Categories
        RealmResults<CategoryModel> categoriesRealmList = mDatabase.where(CategoryModel.class)
                .notEqualTo("type", "SUPPORTING").findAll()
                .sort("categoryName");
        categoriesList = mDatabase.copyFromRealm(categoriesRealmList);
        if (categoriesList.size() > 10) {
            categoriesList.subList(10, categoriesList.size()).clear();
        }
        categoriesAdapter = new HomeCategoriesAdapter(categoriesList, getActivity());
        categoriesRV.setAdapter(categoriesAdapter);
        categoriesRV.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        categoriesAdapter.notifyDataSetChanged();
        categoriesMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MORE Clicked - Take user to Categories Fragment
                Log.i(TAG, "onClick: Categories More");
                if (mMainActivity != null)
                    mMainActivity.changeFragment(CategoriesFragment.newInstance());
            }
        });
        if (categoriesList.size() == 0) {
            view.findViewById(R.id.home_categories_none_text_view).setVisibility(View.VISIBLE);
        }

        //Display Events of current day
        Calendar cal = Calendar.getInstance();
        Calendar day1 = new GregorianCalendar(2018, 9, 3);
        Calendar day2 = new GregorianCalendar(2018, 9, 4);
        Calendar day3 = new GregorianCalendar(2018, 9, 5);
        Calendar day4 = new GregorianCalendar(2018, 9, 6);
        Calendar curDay = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        int dayOfEvent;

        /*if(curDay.getTimeInMillis() < day1.getTimeInMillis()){
            dayOfEvent =0;
        }else */
        if (curDay.getTimeInMillis() < day2.getTimeInMillis()) {
            dayOfEvent = 1;
        } else if (curDay.getTimeInMillis() < day3.getTimeInMillis()) {
            dayOfEvent = 2;
        } else if (curDay.getTimeInMillis() < day4.getTimeInMillis()) {
            dayOfEvent = 3;
        } else {
            dayOfEvent = 4;
        }

        String sortCriteria[] = {"day", "startTime", "eventName"};
        Sort sortOrder[] = {Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING};

//        //PreRevels events
//        if (dayOfEvent == 0) {
//            try {
//                List<ScheduleModel> eventsRealmResults = mDatabase.copyFromRealm((mDatabase.where(ScheduleModel.class).findAll()));
//                for (int i = 0; i < eventsRealmResults.size(); i++) {
//                    Log.d(TAG, "dayFilter Value: " + eventsRealmResults.get(i).getIsRevels());
//                    if (eventsRealmResults.get(i).getIsRevels().contains("0")) {
//                        eventsList.add(eventsRealmResults.get(i));
//                        if (isFavourite(eventsRealmResults.get(i))) {
//                            eventsList.remove(eventsRealmResults.get(i));
//                            eventsList.add(0, eventsRealmResults.get(i));
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        //Main Tech Tatva Events
//        else {
            eventsList.clear();
            eventsList = mDatabase.copyFromRealm(mDatabase.where(ScheduleModel.class)
                    /*.equalTo("isRevels", "1")*/.equalTo("day", dayOfEvent + "")
                    .findAll().sort(sortCriteria, sortOrder));
            for (int i = 0; i < eventsList.size(); i++) {
                ScheduleModel event = eventsList.get(i);
                if (isFavourite(event)) {
                    //Move to top if the event is a Favourite
                    eventsList.remove(event);
                    eventsList.add(0, event);
                }
            }
        //}
//        }
        if (eventsList.size() > 10) {
            eventsList.subList(10, eventsList.size()).clear();
        }
        HomeEventsAdapter.EventLongPressListener longPressListener = new HomeEventsAdapter.EventLongPressListener() {
            @Override
            public void onItemLongPress(ScheduleModel event) {

                registerForEvent(event.getEventId());
            }
        };
        eventsAdapter = new HomeEventsAdapter(eventsList, null, longPressListener,getActivity());
        Log.i(TAG, "onCreateView: eventsList size" + eventsList.size());
        eventsRV.setAdapter(eventsAdapter);
        eventsRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        eventsAdapter.notifyDataSetChanged();
        eventsMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MORE Clicked - Take user to Events Fragment
                Log.i(TAG, "onClick: Events More");
                if (mMainActivity != null)
                    mMainActivity.changeFragment(EventsFragment.newInstance());
            }
        });
        if (eventsList.size() == 0) {
            view.findViewById(R.id.home_events_none_text_view).setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean isConnectedTemp = NetworkUtils.isInternetConnected(getContext());
                if (isConnectedTemp) {
                    displayInstaFeed();
                    fetchResults();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 5000);
                } else {
                    Snackbar.make(view, "Check connection!", Snackbar.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });
        return view;
    }

    private void sliderInit() {   //Updating the SliderLayout with images
        //Animation type
        imageSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        //Setting the Transition time and Interpolator for the Animation
        imageSlider.setSliderTransformDuration(200, new AccelerateDecelerateInterpolator());
        imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        imageSlider.setCustomAnimation(new DescriptionAnimation());
        //Setting the time after which it moves to the next image
        imageSlider.setDuration(400);
        imageSlider.setVisibility(View.GONE);
    }

    public void displayInstaFeed() {
        if (initialLoad) progressBar.setVisibility(View.VISIBLE);
        homeRV.setVisibility(View.GONE);
        instaTextView.setVisibility(View.GONE);
        Call<InstagramFeed> call = InstaFeedAPIClient.getInterface().getInstagramFeed();
        processes++;
        call.enqueue(new Callback<InstagramFeed>() {
            @Override
            public void onResponse(@NonNull Call<InstagramFeed> call, @NonNull Response<InstagramFeed> response) {
                if (initialLoad) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    feed = response.body();
                    instaAdapter = new HomeAdapter(feed);
                    homeRV.setAdapter(instaAdapter);
                    homeRV.setLayoutManager(new LinearLayoutManager(getContext()));
                    ViewCompat.setNestedScrollingEnabled(homeRV, false);
                }
                homeRV.setVisibility(View.VISIBLE);
                initialLoad = false;

            }

            @Override
            public void onFailure(@NonNull Call<InstagramFeed> call, @NonNull Throwable t) {
                if (initialLoad) progressBar.setVisibility(View.GONE);
                instaTextView.setVisibility(View.VISIBLE);
                Log.i(TAG, "onFailure: Error Fetching insta feed ");
                initialLoad = false;
            }
        });
    }

    public void updateResultsList() {
        RealmResults<ResultModel> results = mDatabase.where(ResultModel.class)
                .findAll().sort("eventName", Sort.ASCENDING, "position", Sort.ASCENDING);
        List<ResultModel> resultsArrayList = mDatabase.copyFromRealm(results);
        if (!resultsArrayList.isEmpty()) {
            resultsList.clear();
            List<String> eventNamesList = new ArrayList<>();
            for (ResultModel result : resultsArrayList) {
                String eventName = result.getEventName() + " " + result.getRound();
                if (eventNamesList.contains(eventName)) {
                    resultsList.get(eventNamesList.indexOf(eventName)).eventResultsList.add(result);
                } else {
                    EventResultModel eventResult = new EventResultModel();
                    eventResult.eventName = result.getEventName();
                    eventResult.eventRound = result.getRound();
                    eventResult.eventCategory = result.getCatName();
                    eventResult.eventResultsList.add(result);
                    resultsList.add(eventResult);
                    eventNamesList.add(eventName);
                }
            }
        }
        Log.i(TAG, "displayResults: resultsList size:" + resultsList.size());
        //Moving favourite results to top
        for (int i = 0; i < resultsList.size(); i++) {
            EventResultModel result = resultsList.get(i);
            if (isFavourite(result)) {
                resultsList.remove(result);
                resultsList.add(0, result);
            }
        }
        //Picking first 10 results to display
        if (resultsList.size() > 10) {
            resultsList.subList(10, resultsList.size()).clear();
        }
        resultsAdapter.notifyDataSetChanged();

        if (resultsList.size() == 0) {
            homeResultsItem.setVisibility(View.GONE);
        } else {
            homeResultsItem.setVisibility(View.VISIBLE);
        }
    }
    private void launchCCT(String url, Context context){

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.mitpost));
// set toolbar color and/or setting custom actions before invoking build()
// Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
        CustomTabsIntent customTabsIntent = builder.build();
// and launch the desired Url with CustomTabsIntent.launchUrl()
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

//    private void getImageURLSfromFirebase() {
//        long cacheExpiration = 3600;
//        firebaseRemoteConfig.fetch(cacheExpiration)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        List<String> imgURLs = new ArrayList<>();
//                        final List<String> linkURLs = new ArrayList<>();
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "onComplete: Successful");
//                            firebaseRemoteConfig.activateFetched();
//                            int n_banners;
//                            try {
//                                n_banners = Integer.parseInt(firebaseRemoteConfig.getString("num_banners"));
//                            } catch (Exception e) {
//                                n_banners = 1;
//                            }
//                            Log.d(TAG, "n banners: " + n_banners);
//                            for (int i = 1; i <= n_banners; i++) {
//                                String imgURL = firebaseRemoteConfig.getString("banner_img_" + i);
//                                String linkURL = firebaseRemoteConfig.getString("banner_link_" + i);
//
//                                imgURLs.add(imgURL);
//                                linkURLs.add(linkURL);
//                                Log.d(TAG, "onComplete: img:" + imgURL + " \nLink:" + linkURL);
//                            }
//
//                        } else {
//                            //Unable to fetch Config Values from Firebase.
//
//                            Log.d(TAG, "onComplete: Default" + task.getException().toString());
//                        }
//                        BaseSliderView.ScaleType imgScaleType = BaseSliderView.ScaleType.CenterCrop;
//                        if (imgURLs.size() != linkURLs.size() || imgURLs.size() == 0 || linkURLs.size() == 0) {
//                            return;
//                        }
//                        for (int i = 0; i < imgURLs.size(); i++) {
//                            TextSliderView tsv = new TextSliderView(getContext());
//                            final String hyperlink = linkURLs.get(i);
//                            tsv.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
//                                @Override
//                                public void onSliderClick(BaseSliderView slider) {
//                                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                                    intent.setData(Uri.parse(hyperlink));
//                                    startActivity(intent);
//                                }
//                            });
//                            tsv.image(imgURLs.get(i));
//                            tsv.setScaleType(imgScaleType);
//                            if (imageSlider == null) {
//                                Log.d(TAG, "onComplete: NullCheck Called");
//                                imageSlider = v.findViewById(R.id.home_image_slider);
//                                sliderInit();
//                            }
//                            imageSlider.addSlider(tsv);
//                        }
//                        imageSlider.setVisibility(View.VISIBLE);
//                    }
//                });
//
//    }

    public void fetchResults() {
        processes++;
        try {
            Call<ResultsListModel> callResultsList = APIClient.getAPIInterface().getResultsList();
            callResultsList.enqueue(new Callback<ResultsListModel>() {
                List<ResultModel> results = new ArrayList<>();

                @Override
                public void onResponse(@NonNull Call<ResultsListModel> call, @NonNull Response<ResultsListModel> response) {
                    if (response.isSuccessful() && response.body() != null && mDatabase!=null) {
                        results = response.body().getData();
                        mDatabase.beginTransaction();
                        mDatabase.where(ResultModel.class).findAll().deleteAllFromRealm();
                        mDatabase.copyToRealm(results);
                        mDatabase.commitTransaction();
                        //homeResultsItem.setVisibility(View.VISIBLE);
                        updateResultsList();
                        resultsNone.setVisibility(View.GONE);
                        resultsNone.setText("");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResultsListModel> call, @NonNull Throwable t) {
                    if (homeResultsItem.getVisibility() == View.VISIBLE)
                        //homeResultsItem.setVisibility(View.GONE);
                        processes--;
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isFavourite(ScheduleModel event) {
        RealmResults<FavouritesModel> favouritesRealmList = mDatabase.where(FavouritesModel.class).equalTo("id", event.getEventId()).contains("day", event.getDay()).equalTo("round", event.getRound()).findAll();
        return (favouritesRealmList.size() != 0);
    }

    public boolean isFavourite(EventResultModel result) {
        RealmResults<FavouritesModel> favouritesRealmList = mDatabase.where(FavouritesModel.class).equalTo("eventName", result.eventName).findAll();
        return (favouritesRealmList.size() != 0);
    }

    public View initViews(LayoutInflater inflater, ViewGroup container) {

        appBarLayout = container.findViewById(R.id.app_bar);
        navigation = container.findViewById(R.id.navigation);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        homeRV = view.findViewById(R.id.home_recycler_view);
        resultsRV = view.findViewById(R.id.home_results_recycler_view);
        categoriesRV = view.findViewById(R.id.home_categories_recycler_view);
        eventsRV = view.findViewById(R.id.home_events_recycler_view);
        resultsMore = view.findViewById(R.id.home_results_more_text_view);
        categoriesMore = view.findViewById(R.id.home_categories_more_text_view);
        eventsMore = view.findViewById(R.id.home_events_more_text_view);
        resultsNone = view.findViewById(R.id.home_results_none_text_view);
        homeResultsItem = view.findViewById(R.id.home_results_frame);
        instaTextView = view.findViewById(R.id.instagram_textview);
        swipeRefreshLayout = view.findViewById(R.id.home_swipe_refresh_layout);
        imageSlider = view.findViewById(R.id.home_image_slider);
        if (imageSlider == null) {
            Log.d(TAG, "initViews: Null imageSlider");
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile: {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (sp.getBoolean("loggedIn", false))
                    startActivity(new Intent(getActivity(), ProfileActivity.class));
                else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return true;
            }
            case R.id.menu_favourites: {
                startActivity(new Intent(getActivity(), FavouritesActivity.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
//        if (imageSlider == null) {
//            Log.d(TAG, "onResume: called");
//            imageSlider = v.findViewById(R.id.home_image_slider);
//            getImageURLSfromFirebase();
//            sliderInit();
//        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called");
        if (imageSlider != null) {
            imageSlider.removeAllSliders();
            imageSlider.stopAutoCycle();
            imageSlider = null;
        }
        super.onStop();
    }

    private void showUpdateAlert(){
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
         final AlertDialog updateDialog=new AlertDialog.Builder(getActivity()).create();
         if(!sp.getBoolean("displayedLongPressHint",false)) {
             updateDialog.setIcon(R.drawable.ic_success);
             updateDialog.setTitle("What's new");
             updateDialog.setCancelable(false);
             updateDialog.setMessage("You can now register for events in-app without QR by long pressing on an event !");
             updateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     sp.edit().putBoolean("displayedLongPressHint", true).apply();
                     Log.d(TAG, "onClickUpdateAlert: " + sp.getBoolean("displayedLongPressHint", false));
                     updateDialog.dismiss();
                 }
             });
             updateDialog.show();
         }
    }

    private void registerForEvent(String eventID) {
        Log.d(TAG, "registerForEvent: called");
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Trying to register you for event... please wait!");
        dialog.setCancelable(false);
        dialog.show();
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "eventid=" + eventID);
        Call<EventRegistrationResponse> call = RegistrationClient.getRegistrationInterface(getContext()).eventReg(RegistrationClient.generateCookie(getContext()), body);
        call.enqueue(new Callback<EventRegistrationResponse>() {
            @Override
            public void onResponse(Call<EventRegistrationResponse> call, Response<EventRegistrationResponse> response) {
                dialog.cancel();
                if (response != null && response.body() != null) {
                    try {
                        showAlert(response.body().getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
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
        try {
            new AlertDialog.Builder(getContext()).setIcon(R.drawable.ic_info).setTitle("Info").setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).setCancelable(true).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }
}
