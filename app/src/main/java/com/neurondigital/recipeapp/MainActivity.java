package com.neurondigital.recipeapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IabBroadcastReceiver;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the first Activity shown.
 * <p>
 * Handles the generation of the side navigation drawer, shows the main fragment and shows ads if enabled
 */

public class MainActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener, FragmentManager.OnBackStackChangedListener {

    Toolbar toolbar;
    Drawer drawer;
    Fragment fragment;
    Context context;

    //ad related
    AdView ad;
    LinearLayout BackgroundLayout;
    AdvertHelper advertHelper;
    int ad_counter = 0;

    //Analytics
    AnalyticsHelper analyticsHelper;

    //billing
    BillingHelper billingHelper;

    //navigation drawer item identification numbers
    final int NAV_HOME = 0, NAV_FAV = 1, NAV_SHOP = 2, NAV_MORE = 3, NAV_INFO = 4, NAV_PREMIUM = 5, NAVSETTINGS = 6, NAV_CATEGORIES = 100, NAV_ADD_RECIPE = 7;
    private boolean doubleBackToExitPressedOnce = false;
    private TextView mTitle;


    public void setTitle(String title) {
        mTitle.setText(title);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Configuration configuration = getResources().getConfiguration();
//        configuration.setLayoutDirection(new Locale("fa"));
//        configuration.setLocale(new Locale("fa"));
//        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);
        this.context = this;

        //portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //enable/disable Firebase topic subscription
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("pref_enable_push_notifications", true))
            FirebaseMessaging.getInstance().subscribeToTopic(Configurations.FIREBASE_PUSH_NOTIFICATION_TOPIC);
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Configurations.FIREBASE_PUSH_NOTIFICATION_TOPIC);

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        //Generate the side navigation drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
//                .withToolbar(toolbar)
                .withRootView(R.id.drawer_container)
                .withDisplayBelowStatusBar(true)
//                .withActionBarDrawerToggle(true)
//                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(getDrawerItems(null))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //On click: open the required activity or fragment
                        Intent intent;
                        switch ((int) drawerItem.getIdentifier()) {
                            case NAV_HOME:
                                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                break;
//                            case NAV_MORE:
//                                this can be set through configurations.jave
//                                if (Configurations.CATEGORY_MENU_TYPE == Configurations.CATEGORY_GRID_TEXT_AND_IMAGE)
//                                    changeFragment(new CategoryGridTextAndImageFragment());
//                                else if (Configurations.CATEGORY_MENU_TYPE == Configurations.CATEGORY_TEXT_ONLY)
//                                    changeFragment(new CategoriesFragment());
//                                else if (Configurations.CATEGORY_MENU_TYPE == Configurations.CATEGORY_TEXT_AND_IMAGE)
//                                    changeFragment(new CategoryTextAndImageFragment());
//                                else
//                                    changeFragment(new CategoriesFragment());
//                                break;
                            case NAV_FAV:
                                changeFragment(new FavoriteFragment(), true);
                                break;
//                            case NAV_SHOP:
//                                changeFragment(new ShoppingListFragment());
//                                break;
                            case NAV_INFO:
                                changeFragment(new InfoFragment(), true);
                                break;
                            case NAV_PREMIUM:
                                intent = new Intent(context, PremiumActivity.class);
                                startActivity(intent);
                                break;
                            case NAVSETTINGS:
                                intent = new Intent(context, SettingsActivity.class);
                                startActivity(intent);
                                break;
                            case NAV_ADD_RECIPE:
                                changeFragment(new AddRecipeFragment(), true);
                                break;
//                            default:
//                                opens the categories displayed in drawer
//                                if (drawerItem.getIdentifier() > NAV_CATEGORIES) {
//                                    Bundle b = new Bundle();
//                                    b.putInt("Category_id", (int) (drawerItem.getIdentifier() - NAV_CATEGORIES));
//                                    Fragment f = new CategoryRecipesFragment();
//                                    f.setArguments(b);
//                                    changeFragment(f);
//                                }
                        }
                        drawer.closeDrawer();
                        return true;
                    }
                })
                .build();

//initialise analytics
        analyticsHelper = new AnalyticsHelper(this);
        analyticsHelper.initialiseAnalytics(getResources().getString(R.string.google_analytics_id));

        //add Google Analytics view
        analyticsHelper.AnalyticsView();

        //initialise billing
        billingHelper = new BillingHelper(this,
                new BillingHelper.RefreshListener() {
                    @Override
                    public void onRefresh(boolean isPremium, Inventory inventory) {
                        if (isPremium) {
                            //destroy banner ad
                            if (ad != null)
                                ad.destroy();
                            if (BackgroundLayout != null)
                                BackgroundLayout.removeView(ad);

                            //remove upgrade 'Go premium' from drawer
                            drawer.removeItem(NAV_PREMIUM);

                            //remove purchase
                            invalidateOptionsMenu();
                        }
                    }
                },
                new IabHelper.OnIabPurchaseFinishedListener() {
                    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                        System.out.println("Purchase successful " + result);
                    }
                },
                new IabHelper.OnConsumeFinishedListener() {
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                    }
                }, Configurations.PUBLIC_KEY);


        // Important: Dynamically register for broadcast messages about updated purchases.
        // We register the receiver here instead of as a <receiver> in the Manifest
        // because we always call getPurchases() at startup, so therefore we can ignore
        // any broadcasts sent while the app isn't running.
        // Note: registering this listener in an Activity is a bad idea, but is done here
        // because this is a SAMPLE. Regardless, the receiver must be registered after
        // IabHelper is setup, but before first call to getPurchases().
        billingHelper.mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
        IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
        registerReceiver(billingHelper.mBroadcastReceiver, broadcastFilter);


        //Admob Banner and Interstitial Advert
        BackgroundLayout = (LinearLayout) findViewById(R.id.background_layout);
        if (!billingHelper.isPremium()) {
            advertHelper = new AdvertHelper(this, getResources().getString(R.string.interstitial_ad), null);
            advertHelper.initialiseInterstitialAd();
            final AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("275D94C2B5B93B3C4014933E75F92565")///nexus7//////testing
                    .addTestDevice("91608B19766D984A3F929C31EC6AB947")
                    .addTestDevice("6316D285813B01C56412DAF4D3D80B40") ///test htc sensesion xl
                    .addTestDevice("8C416F4CAF490509A1DA82E62168AE08")//asus transformer
                    .addTestDevice("7B4C6D080C02BA40EF746C4900BABAD7")//Galaxy S4
                    .addTestDevice("EA8AA9C3AA2BD16A954F592C6F935628")//motorola moto G
                    .addTestDevice("2278D56057803B605C31511DBFBEE7DF")//S7 edge
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

            ad = (AdView) findViewById(R.id.adView);
            if (getResources().getString(R.string.banner_ad).length() > 1) {
                ad.loadAd(adRequest);
            } else {
                if (ad != null)
                    ad.destroy();
                if (BackgroundLayout != null)
                    BackgroundLayout.removeView(ad);
            }
        }

        Category.loadCategories(context, "", new Category.onCategoriesDownloadedListener() {
            @Override
            public void onCategoriesDownloaded(List<Category> categories) {
                refreshNavDrawer(categories);
            }
        });
        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        //Handle when activity is recreated like on orientation Change
        changeFragment(new CategoryGridTextAndImageFragment(), false);

    }


    /**
     * Removes all items from drawer and creates them again to refresh.
     *
     * @param categories - List of Categories
     */
    public void refreshNavDrawer(List<Category> categories) {
        drawer.removeAllItems();
        drawer.addItems(getDrawerItems(categories));
    }

    /**
     * Generates a list of Drawer items
     *
     * @param categories
     * @return
     */
    public IDrawerItem[] getDrawerItems(List<Category> categories) {
        List<IDrawerItem> drawerItems = new ArrayList<>();

        //Add Home, Favorites and Shopping list
        drawerItems.add(new PrimaryDrawerItem().withIdentifier(NAV_HOME).withName(R.string.nav_home).withIcon(FontAwesome.Icon.faw_home));
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_FAV).withName(R.string.nav_favorites).withIcon(FontAwesome.Icon.faw_star));
//        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_SHOP).withName(R.string.nav_shopping_list).withIcon(FontAwesome.Icon.faw_shopping_cart));

//        if (Configurations.DISPLAY_CATEGORIES_IN_NAVIGATION_DRAWER) {
//            //Add categories and more...
//            drawerItems.add(new SectionDrawerItem().withName(R.string.nav_categories));
//            if (categories != null) {
//                for (int i = 0; i < categories.size(); i++) {
//                    if (i < getResources().getInteger(R.integer.categories_to_show_in_drawer))
//                        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_CATEGORIES + categories.get(i).id).withName(categories.get(i).name).withIcon(FontAwesome.Icon.faw_cutlery));
//                }
//            }
//            drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_MORE).withName(R.string.nav_categories_more).withIcon(FontAwesome.Icon.faw_ellipsis_h));
//            drawerItems.add(new DividerDrawerItem());
//        } else {
//            //add just a categories button
//            drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_MORE).withName(R.string.nav_categories).withIcon(FontAwesome.Icon.faw_bars));
//        }

        //add final 4 items
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_ADD_RECIPE).withName(R.string.nav_add_recipe).withIcon(FontAwesome.Icon.faw_plus));
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_INFO).withName(R.string.nav_info).withIcon(FontAwesome.Icon.faw_question));
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_PREMIUM).withName(R.string.nav_go_premium).withIcon(FontAwesome.Icon.faw_money));
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAVSETTINGS).withName(R.string.nav_settings).withIcon(FontAwesome.Icon.faw_cog));

        return drawerItems.toArray(new IDrawerItem[0]);
    }

    /**
     * Open interstitial Ad every couple of times. The number of clicks can be set from strings.xml
     * Doesn't display ads in premium mode.
     *
     * @return
     */
    public boolean loadInterstitial() {
        if (!billingHelper.isPremium()) {
            ad_counter++;
            if (ad_counter >= getResources().getInteger(R.integer.ad_shows_after_X_clicks)) {
                advertHelper.openInterstitialAd(new AdvertHelper.InterstitialListener() {
                    @Override
                    public void onClosed() {
                    }

                    @Override
                    public void onNotLoaded() {

                    }
                });

                ad_counter = 0;
                return true;
            }
        }
        return false;
    }

    /**
     * Change main fragment
     *
     * @param fragment
     * @param addToBackStack
     */
    public void changeFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFragment, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        } else
            this.fragment = fragment;

        transaction.commit();
    }


    /**
     * On back pressed, always go to home fragment before closing
     */
    @Override
    public void onBackPressed() {

        //if stack has items left
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (!doubleBackToExitPressedOnce) {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Broadcast receiver for billing
     */
    @Override
    public void receivedBroadcast() {
        billingHelper.receivedBroadcast();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //back from billing
        if (billingHelper.onActivityResult(requestCode, resultCode, data)) {

        }

    }


    @Override
    public void onStart() {
        super.onStart();
        //analytics
        analyticsHelper.onStart();
    }

    @Override
    public void onPause() {
        ad.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        ad.resume();
        if (billingHelper != null)
            billingHelper.refreshInventory();
    }

    @Override
    public void onDestroy() {
        ad.destroy();
        super.onDestroy();
        if (billingHelper.mBroadcastReceiver != null) {
            unregisterReceiver(billingHelper.mBroadcastReceiver);
        }
        if (billingHelper != null)
            billingHelper.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //analytics
        analyticsHelper.onStop();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle:
                if (drawer == null)
                    return false;
                if (drawer.isDrawerOpen())
                    drawer.closeDrawer();
                else
                    drawer.openDrawer();
                break;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            // newText is text entered by user to SearchView
            if (fragment != null) {
                if (fragment instanceof CategoryGridTextAndImageFragment) {
                    ((CategoryGridTextAndImageFragment) fragment).refresh(newText);
                    return true;
                } else if (fragment instanceof RecipesFragment) {
                    ((RecipesFragment) fragment).refresh(newText);
                    return true;
                }
            }
            return false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.options_menu, menu);
//        menu.findItem(R.id.search).setIcon(
//                new IconicsDrawable(getBaseContext())
//                        .icon(FontAwesome.Icon.faw_search)
//                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
//                        .sizeDp(18));
        try {
            // Associate searchable configuration with the SearchView
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.search).getActionView();
//            searchView.set
            searchView.setOnQueryTextListener(listener);


        } catch (Exception e) {

        }
        return true;
    }


    @Override
    public void onBackStackChanged() {
        fragment = getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

}
