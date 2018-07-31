package in.mittt.tt18.fragments;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.appyvet.rangebar.IRangeBarFormatter;
import com.appyvet.rangebar.RangeBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import in.mittt.tt18.R;
import in.mittt.tt18.adapters.EventsTabsPagerAdapter;
import in.mittt.tt18.adapters.FilterVenueAdapter;
import in.mittt.tt18.application.TT18;
import in.mittt.tt18.models.categories.CategoryModel;
import in.mittt.tt18.models.events.ScheduleModel;
import io.realm.Realm;
import io.realm.RealmResults;


public class EventsFragment extends Fragment {
    Realm mDatabase = Realm.getDefaultInstance();
    View result;
    ViewPager viewPager;
    boolean recyclerDisplayed = false;

    RealmResults<ScheduleModel> scheduleResult;
    String categoryFilterTerm = "All";
    String venueFilterTerm = "All";
    String startTimeFilterTerm = "12:00";
    String endTimeFilterTerm = "9:00";
    int startTimeFilterPin = 0;
    int endTimeFilterPin = 9;

    String remCategoryFilterTerm;
    String remVenueFilterTerm;
    String remStartTimeFilterTerm;
    String remEndTimeFilterTerm;
    int remStartTimeFilterPin;
    int remEndTimeFilterPin;

    boolean filterMode = false;

    private MenuItem searchItem;
    private MenuItem filterItem;
    private ArrayList<String> categoryNames = new ArrayList<>();
    private List<String> venueList = new ArrayList<>();


    public EventsFragment() {
    }

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Events");
        setHasOptionsMenu(true);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().findViewById(R.id.toolbar).setElevation(0);
                AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar);
                appBarLayout.setElevation(0);
                appBarLayout.setTargetElevation(0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        scheduleResult = mDatabase.where(ScheduleModel.class).distinct("venue").findAll().sort("venue");
        venueList.add("All");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        result = inflater.inflate(R.layout.fragment_events, container, false);
        viewPager = result.findViewById(R.id.events_view_pager);
        TabLayout tabLayout = result.findViewById(R.id.events_tab_layout);
        viewPager.setAdapter(new EventsTabsPagerAdapter(getChildFragmentManager(), "", "All", "All", "12:00 pm", "9:00 pm", false));
        tabLayout.setupWithViewPager(viewPager);
        //Set the Tab to the current day-
        Calendar cal = Calendar.getInstance();
        Calendar day2 = new GregorianCalendar(2017, 9, 5);
        Calendar day3 = new GregorianCalendar(2017, 9, 6);
        Calendar day4 = new GregorianCalendar(2017, 9, 7);
        Calendar curDay = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        int day;

        if (curDay.getTimeInMillis() < day2.getTimeInMillis()) {
            day = 0;
        } else if (curDay.getTimeInMillis() < day3.getTimeInMillis()) {
            day = 1;
        } else if (curDay.getTimeInMillis() < day4.getTimeInMillis()) {
            day = 2;
        } else {
            day = 3;
        }

        viewPager.setCurrentItem(day);
        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_hardware, menu);
        searchItem = menu.findItem(R.id.action_search);
        filterItem = menu.findItem(R.id.menu_filter);

        filterItem = menu.findItem(R.id.menu_filter);

        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                final View view = View.inflate(getActivity(), R.layout.dialog_filter, null);
                final Button applyFilter = view.findViewById(R.id.apply_filter);
                final Button clearFilter = view.findViewById(R.id.clear_filter);
                final TextView filterCategories = view.findViewById(R.id.filter_categories);
                final TextView filterTimeRange = view.findViewById(R.id.filter_time_range);
                final RangeBar rangeBar = view.findViewById(R.id.filter_range_bar);
                final TextView filterVenue = view.findViewById(R.id.filter_venue);

                for (ScheduleModel schedule : scheduleResult) {
                    String venue = "";
                    String temp = schedule.getVenue();
                    for (int i = 0; i < temp.length(); i++) {
                        if (temp.charAt(i) == '-') continue;

                        if (!Character.isDigit(temp.charAt(i))) {
                            venue += temp.charAt(i);
                        } else if (i > 0 && temp.charAt(i - 1) != ' ' && temp.charAt(i - 1) != '-') {
                            venue += temp.charAt(i);
                        } else break;
                    }
                    if (!venue.isEmpty() && !venueList.contains(venue))
                        venueList.add(venue);
                }
                if (!filterMode) {
                    filterCategories.setText("All");
                    filterTimeRange.setText("12:00PM to 9:00PM");
                    filterVenue.setText("All");
                    rangeBar.setRangePinsByIndices(0, 9);
                } else {
                    filterCategories.setText(categoryFilterTerm);
                    filterTimeRange.setText(startTimeFilterTerm + "PM to " + endTimeFilterTerm + "PM");
                    filterVenue.setText(venueFilterTerm);
                    rangeBar.setRangePinsByIndices(startTimeFilterPin, endTimeFilterPin);
                }

                bottomSheetDialog.setContentView(view);

                clearFilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filterCategories.setText("All");
                        filterTimeRange.setText("12:00PM to 9:00PM");
                        filterVenue.setText("All");
                        rangeBar.setRangePinsByIndices(0, 9);
                        bottomSheetDialog.dismiss();
                        int item = viewPager.getCurrentItem();
                        viewPager.getAdapter().notifyDataSetChanged();
                        viewPager.setAdapter(new EventsTabsPagerAdapter(getChildFragmentManager(), "", "All", "All", "12:00 pm", "9:00 pm", false));
                        viewPager.getAdapter().notifyDataSetChanged();
                        viewPager.setCurrentItem(item, false);
                        filterMode = false;
                    }
                });

                applyFilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        categoryFilterTerm = filterCategories.getText().toString();
                        venueFilterTerm = filterVenue.getText().toString();
                        int item = viewPager.getCurrentItem();
                        viewPager.getAdapter().notifyDataSetChanged();
                        viewPager.setAdapter(new EventsTabsPagerAdapter(getChildFragmentManager(), "", categoryFilterTerm, venueFilterTerm, startTimeFilterTerm + " pm", endTimeFilterTerm + " pm", true));
                        viewPager.getAdapter().notifyDataSetChanged();
                        viewPager.setCurrentItem(item, false);
                        bottomSheetDialog.dismiss();
                        filterMode = true;
                    }
                });
                RealmResults<CategoryModel> categoryResults = mDatabase.where(CategoryModel.class).findAll().sort("categoryName");
                categoryNames.add("All");
                for (CategoryModel category : categoryResults) {
                    categoryNames.add(category.getCategoryName());
                }
                RecyclerView recyclerView = view.findViewById(R.id.filter_recycler);
                recyclerView.removeAllViews();
                recyclerView.removeAllViewsInLayout();
                FilterVenueAdapter adapter = new FilterVenueAdapter(venueList, getContext(), filterVenue);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                recyclerView.setAdapter(adapter);
                recyclerDisplayed = true;
                adapter.notifyDataSetChanged();
                CardView filterCategoriesCard = view.findViewById(R.id.filter_categories_card);
                filterCategoriesCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view1 = View.inflate(getActivity(), R.layout.dialog_filter_category_select, null);
                        final Dialog d = new Dialog(getContext());
                        Rect displayRectangle = new Rect();
                        Window window = getActivity().getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                        view1.setMinimumWidth((int) (displayRectangle.width() * 0.5f));
                        d.setContentView(view1);

                        final NumberPicker numberPicker = view1.findViewById(R.id.numberPicker);
                        Button button = view1.findViewById(R.id.button);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                filterCategories.setText(categoryNames.get(numberPicker.getValue()));
                                d.dismiss();
                            }
                        });
                        numberPicker.setMinValue(0);
                        numberPicker.setMaxValue(categoryNames.size() - 1);
                        numberPicker.setWrapSelectorWheel(false);
                        numberPicker.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                filterCategories.setText(categoryNames.get(numberPicker.getValue()));
                                d.dismiss();
                            }
                        });
                        numberPicker.setFormatter(new NumberPicker.Formatter() {
                            @Override
                            public String format(int value) {
                                return categoryNames.get(value);
                            }
                        });
                        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                            @Override
                            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                                filterCategories.setText(categoryNames.get(newVal));
                            }
                        });
                        d.show();
                    }
                });

                rangeBar.setFormatter(new IRangeBarFormatter() {
                    @Override
                    public String format(String value) {
                        String time;
                        if (Integer.parseInt(value) == 0) {
                            time = "12:00";
                        } else {
                            time = value + ":00";
                        }
                        return time;
                    }
                });

                rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
                    @Override
                    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                        String startTime;
                        String endTime;

                        if (leftPinIndex == 0) {
                            startTime = "12:00";
                        } else {
                            startTime = leftPinValue + ":00";
                        }
                        if (rightPinIndex == 0) {
                            endTime = "12:00";
                        } else {
                            endTime = rightPinValue + ":00";
                        }

                        startTimeFilterTerm = startTime;
                        endTimeFilterTerm = endTime;
                        startTimeFilterPin = leftPinIndex;
                        endTimeFilterPin = rightPinIndex;
                        filterTimeRange.setText(startTime + "PM to " + endTime + "PM");
                    }
                });
                bottomSheetDialog.show();
                return false;
            }
        });


        final SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        View v = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        v.setBackgroundColor(Color.parseColor("#00000000"));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                text = text.toUpperCase();
                viewPager.getAdapter().notifyDataSetChanged();
                int item = viewPager.getCurrentItem();
                viewPager.setAdapter(new EventsTabsPagerAdapter(getChildFragmentManager(), text, "All", "All", "12:00 pm", "9:00 pm", false));
                viewPager.getAdapter().notifyDataSetChanged();
                viewPager.setCurrentItem(item, false);
                TT18.searchOpen = 2;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                text = text.toUpperCase();
                viewPager.getAdapter().notifyDataSetChanged();
                int item = viewPager.getCurrentItem();
                viewPager.setAdapter(new EventsTabsPagerAdapter(getChildFragmentManager(), text, "All", "All", "12:00 pm", "9:00 pm", false));
                viewPager.getAdapter().notifyDataSetChanged();
                viewPager.setCurrentItem(item, false);
                TT18.searchOpen = 2;
                return false;
            }
        });
        searchView.setQueryHint("Search Events");
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                int item = viewPager.getCurrentItem();
                viewPager.setAdapter(new EventsTabsPagerAdapter(getChildFragmentManager(), "", "All", "All", "12:00", "9:00", false));
                searchView.clearFocus();
                viewPager.setCurrentItem(item, false);

                TT18.searchOpen = 2;
                return false;
            }


        });
    }
}