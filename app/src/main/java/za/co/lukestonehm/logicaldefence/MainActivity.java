package za.co.lukestonehm.logicaldefence;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity
        implements NavCallback {


    public static final String TAG = "LogicalDefence",
            LONG_PRESS_ACTION = "LogicalDefence.LPA";

    private CharSequence mTitle;
    private String[] sections;

    private int mCurrentSelectedPosition = 0;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerListView;

    NavCallback mCallbacks;
    Menu currentActionViewMenu;

    AppPreferences appPrefs;

    LongPressReceiver lpr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawScreen();
    }

    private void drawScreen() {
        setContentView(R.layout.activity_main);
        mCallbacks = this;
        appPrefs = new AppPreferences(this);

        lpr = new LongPressReceiver(this);
        IntentFilter lprFilter = new IntentFilter(LONG_PRESS_ACTION);
        registerReceiver(lpr, lprFilter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        sections = getResources().getStringArray(R.array.sections);

        Toolbar mActionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mActionBar);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                setDrawerItemSelected((TextView) mDrawerListView.getChildAt(mCurrentSelectedPosition));
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mDrawerListView = (ListView) findViewById(R.id.lv);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                unsetDrawerItemSelected((TextView) parent.getChildAt(mCurrentSelectedPosition));
                setDrawerItemSelected((TextView) view);
                selectItem(position);
            }
        });

        mDrawerListView.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.nav_item,
                R.id.nav_item, sections
        ));

        //TODO: Pref this
        mCurrentSelectedPosition = appPrefs.getSection();

        mTitle = sections[mCurrentSelectedPosition];
        setTitle(mTitle);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        changeSection(mCurrentSelectedPosition);
    }


    private void selectItem(int position) {
        mCurrentSelectedPosition = position;

        mDrawerListView.setItemChecked(position, true);
        mDrawerLayout.closeDrawers();
        mCallbacks.onNavigationDrawerItemSelected(position);

        if (appPrefs == null)
            appPrefs = new AppPreferences(this);

        appPrefs.setSection(position);

        mTitle = sections[position];
        setTitle(mTitle);
    }

    private void changeSection(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    private void setDrawerItemSelected(TextView item) {
        item.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        item.setTextColor(Color.WHITE);
    }

    private void unsetDrawerItemSelected(TextView item) {
        item.setBackgroundColor(Color.TRANSPARENT);
        item.setTextColor(getResources().getColor(R.color.primary_dark_material_dark));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        changeSection(position);
    }

    public void onSectionAttached(int number) {
        if (sections == null)
            sections = getResources().getStringArray(R.array.sections);
        mTitle = sections[number - 1];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        currentActionViewMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        if (id == R.id.action_about) {
            startActivity(new Intent(this, About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void fallacyLongPressCallback(int position) {
        Log.d(TAG, "LongPressReceived: " + position);
        String[] titles = getStringArrByName("fallacies_titles_" + (appPrefs.getSection() + 1));
        Toast.makeText(this, titles[position], Toast.LENGTH_SHORT).show();
        fallacyLongPress(position);
    }

    public void fallacyLongPress(int position) {
        getSupportActionBar().setCustomView(R.layout.fallacy_long_press);
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    }

    private String[] getStringArrByName(String name) {
        int i = getResources().getIdentifier(name, "array",
                this.getPackageName()); // You had used "name"
        return getResources().getStringArray(i);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_main, container, false);

            String[] titles = getStringArrByName("fallacies_titles_" + getArguments().getInt(ARG_SECTION_NUMBER));
            String[] descs = getStringArrByName("fallacies_descs_" + getArguments().getInt(ARG_SECTION_NUMBER));
            String[] examples = getStringArrByName("fallacies_examples_" + getArguments().getInt(ARG_SECTION_NUMBER));

            List<Fallacy> fallacies = generateFallacyList(titles, descs, examples);

            ListView lv = (ListView) v.findViewById(R.id.sec_lv);
            FallacyListAdapter fla = new FallacyListAdapter(getActivity(), fallacies);
            lv.setAdapter(fla);

            return v;
        }

        private String[] getStringArrByName(String name) {
            int i = getResources().getIdentifier(name, "array",
                    getActivity().getApplicationContext().getPackageName()); // You had used "name"
            return getResources().getStringArray(i);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        private List<Fallacy> generateFallacyList(String[] titles, String[] descs, String[] examples) {
            List<Fallacy> list = new ArrayList<>();
            for (int i = 0; i < titles.length; i++) {
                list.add(new Fallacy(i, titles[i], descs[i], examples[i]));
            }
            return list;
        }
    }

}
