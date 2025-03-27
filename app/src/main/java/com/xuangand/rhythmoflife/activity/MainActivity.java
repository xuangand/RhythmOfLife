package com.xuangand.rhythmoflife.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.xuangand.rhythmoflife.R;
import com.xuangand.rhythmoflife.constant.Constant;
import com.xuangand.rhythmoflife.constant.GlobalFunction;
import com.xuangand.rhythmoflife.databinding.ActivityMainBinding;
import com.xuangand.rhythmoflife.fragment.AllSongsFragment;
import com.xuangand.rhythmoflife.fragment.ArtistFragment;
import com.xuangand.rhythmoflife.fragment.CategoryFragment;
import com.xuangand.rhythmoflife.fragment.ChangePasswordFragment;
import com.xuangand.rhythmoflife.fragment.ContactFragment;
import com.xuangand.rhythmoflife.fragment.FavoriteFragment;
import com.xuangand.rhythmoflife.fragment.FeedbackFragment;
import com.xuangand.rhythmoflife.fragment.HomeFragment;
import com.xuangand.rhythmoflife.fragment.PopularSongsFragment;
import com.xuangand.rhythmoflife.fragment.SearchFragment;
import com.xuangand.rhythmoflife.fragment.SongsByArtistFragment;
import com.xuangand.rhythmoflife.fragment.SongsByCategoryFragment;
import com.xuangand.rhythmoflife.fragment.UpgradeFragment;
import com.xuangand.rhythmoflife.model.Artist;
import com.xuangand.rhythmoflife.model.Category;
import com.xuangand.rhythmoflife.model.Song;
import com.xuangand.rhythmoflife.model.User;
import com.xuangand.rhythmoflife.prefs.DataStoreManager;
import com.xuangand.rhythmoflife.service.MusicService;
import com.xuangand.rhythmoflife.utils.AppOpenAdManager;
import com.xuangand.rhythmoflife.utils.GlideUtils;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import vn.zalopay.sdk.ZaloPaySDK;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final int TYPE_HOME = 1;
    public static final int TYPE_CATEGORY = 2;
    public static final int TYPE_ARTIST = 3;
    public static final int TYPE_ALL_SONGS = 4;
    public static final int TYPE_POPULAR_SONGS = 5;
    public static final int TYPE_FAVORITE_SONGS = 6;
    public static final int TYPE_FEEDBACK = 7;
    public static final int TYPE_CONTACT = 8;
    public static final int TYPE_CHANGE_PASSWORD = 9;

    private static final int REQUEST_PERMISSION_CODE = 10;
    public static final int TYPE_UPGRADE = 11;
    private Song mSong;

    private int mTypeScreen = TYPE_HOME;
    private ActivityMainBinding mActivityMainBinding;
    private int mAction;
    //private static final String AD_UNIT_ID = "ca-app-pub-3018892996143210/2836076589";
    //InterstitialAd example: ca-app-pub-3940256099942544/1033173712
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private AppOpenAdManager appOpenAdManager;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAction = intent.getIntExtra(Constant.MUSIC_ACTION, 0);
            handleMusicAction();
        }
    };
    private InterstitialAd mInterstitialAd;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mActivityMainBinding.getRoot());

        checkNotificationPermission();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constant.CHANGE_LISTENER));
        openHomeScreen();
        displayUserInformation();
        checkSubscription();
        initListener();
        displayLayoutBottom();
    }

    private void loadInterstitialAd() {
        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();
        AdRequest adRequest = new AdRequest.Builder().build();
        //"ca-app-pub-3018892996143210/5054373360"
        InterstitialAd.load(this,AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        if (mInterstitialAd != null) {
                            mInterstitialAd.show(MainActivity.this);
                        } else {
                            Log.d("TAG", "The interstitial ad wasn't ready yet.");
                        }
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }
    private void updateUiForSubscription(boolean isPro) {
        // Update UI to reflect subscription status
        if (isPro) {
            Log.d("Subscription", "User is Pro, updating UI...");
            // Show Pro version UI elements
            mActivityMainBinding.menuLeft.tvAppVersion.setText(getString(R.string.label_app_version_pro));
            mActivityMainBinding.menuLeft.layoutMenuUpgrade.setVisibility(View.GONE);
        } else {
            Log.d("Subscription", "User is not Pro.");
            // Keep the Free UI
            mActivityMainBinding.menuLeft.tvAppVersion.setText(getString(R.string.label_app_version_free));
            mActivityMainBinding.menuLeft.layoutMenuUpgrade.setVisibility(View.VISIBLE);
            loadInterstitialAd();
        }
    }

    private void checkSubscription() {
//        GlobalFunction.checkSubscription(isActive -> {
//            if (isActive) {
//                mActivityMainBinding.menuLeft.tvAppVersion.setText(getString(R.string.label_app_version_pro));
//                mActivityMainBinding.menuLeft.layoutMenuUpgrade.setVisibility(View.GONE);
//            }
//        });
        GlobalFunction.checkSubscription(new GlobalFunction.OnSubscriptionCheckListener() {
            @Override
            public void onResult(boolean isPro) {
                updateUiForSubscription(isPro);
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void displayUserInformation() {
        User user = DataStoreManager.getUser();
        mActivityMainBinding.menuLeft.tvUserEmail.setText(user.getEmail());
    }

    private void initHeader() {
        switch (mTypeScreen) {
            case TYPE_HOME:
                handleToolbarTitle(getString(R.string.app_name));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
                break;

            case TYPE_CATEGORY:
                handleToolbarTitle(getString(R.string.menu_category));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
                break;

            case TYPE_ARTIST:
                handleToolbarTitle(getString(R.string.menu_artist));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
                break;

            case TYPE_ALL_SONGS:
                handleToolbarTitle(getString(R.string.menu_all_songs));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(true);
                break;

            case TYPE_POPULAR_SONGS:
                handleToolbarTitle(getString(R.string.menu_popular_songs));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(true);
                break;

            case TYPE_FAVORITE_SONGS:
                handleToolbarTitle(getString(R.string.menu_favorite_songs));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(true);
                break;

            case TYPE_FEEDBACK:
                handleToolbarTitle(getString(R.string.menu_feedback));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
                break;

            case TYPE_CONTACT:
                handleToolbarTitle(getString(R.string.menu_contact));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
                break;

            case TYPE_UPGRADE:
                handleToolbarTitle(getString(R.string.menu_upgrade));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
                break;

            case TYPE_CHANGE_PASSWORD:
                handleToolbarTitle(getString(R.string.menu_change_password));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
                break;
        }
    }

    private void handleToolbarTitle(String title) {
        mActivityMainBinding.header.tvTitle.setText(title);
    }

    public void handleDisplayIconHeader(boolean isShowMenuLeft) {
        if (isShowMenuLeft) {
            mActivityMainBinding.header.imgLeft.setImageResource(R.drawable.ic_menu_left);
            mActivityMainBinding.header.imgLeft.setOnClickListener(
                    v -> mActivityMainBinding.drawerLayout.openDrawer(GravityCompat.START)
            );
        } else {
            mActivityMainBinding.header.imgLeft.setImageResource(R.drawable.ic_back_white);
            mActivityMainBinding.header.imgLeft.setOnClickListener(
                    v -> onBackPressed()
            );
        }
    }

    public void handleDisplayButtonPlayAll(boolean isShow) {
        if (isShow) {
            mActivityMainBinding.header.layoutPlayAll.setVisibility(View.VISIBLE);
        } else {
            mActivityMainBinding.header.layoutPlayAll.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        mActivityMainBinding.header.layoutPlayAll.setOnClickListener(this);

        mActivityMainBinding.menuLeft.layoutClose.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuHome.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuCategory.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuArtist.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuAllSongs.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuPopularSongs.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuFavoriteSongs.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuFeedback.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuContact.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuUpgrade.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuChangePassword.setOnClickListener(this);
        mActivityMainBinding.menuLeft.layoutMenuSignOut.setOnClickListener(this);

        mActivityMainBinding.layoutBottom.imgPrevious.setOnClickListener(this);
        mActivityMainBinding.layoutBottom.imgPlay.setOnClickListener(this);
        mActivityMainBinding.layoutBottom.imgNext.setOnClickListener(this);
        mActivityMainBinding.layoutBottom.imgClose.setOnClickListener(this);
        mActivityMainBinding.layoutBottom.layoutText.setOnClickListener(this);
        mActivityMainBinding.layoutBottom.imgSong.setOnClickListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment instanceof HomeFragment) {
                handleToolbarTitle(getString(R.string.app_name));
                handleDisplayIconHeader(true);
                handleDisplayButtonPlayAll(false);
            } else if (fragment instanceof CategoryFragment) {
                handleToolbarTitle(getString(R.string.menu_category));
                handleDisplayButtonPlayAll(false);
                CategoryFragment categoryFragment = (CategoryFragment) fragment;
                handleDisplayIconHeader(categoryFragment.mIsFromMenuLeft);
            } else if (fragment instanceof ArtistFragment) {
                handleToolbarTitle(getString(R.string.menu_artist));
                handleDisplayButtonPlayAll(false);
                ArtistFragment artistFragment = (ArtistFragment) fragment;
                handleDisplayIconHeader(artistFragment.mIsFromMenuLeft);
            }
        });
    }

    private void openHomeScreen() {
        replaceFragment(new HomeFragment(), "HomeFragment");
        mTypeScreen = TYPE_HOME;
        initHeader();
    }

    public void openCategoryScreen() {
        replaceFragment(CategoryFragment.newInstance(true), "CategoryFragment");
        mTypeScreen = TYPE_CATEGORY;
        initHeader();
    }

    private void openArtistScreen() {
        replaceFragment(ArtistFragment.newInstance(true), "ArtistFragment");
        mTypeScreen = TYPE_ARTIST;
        initHeader();
    }

    private void openAllSongsScreen() {
        replaceFragment(new AllSongsFragment(), "AllSongsFragment");
        mTypeScreen = TYPE_ALL_SONGS;
        initHeader();
    }

    public void openPopularSongsScreen() {
        replaceFragment(new PopularSongsFragment(), "PopularSongsFragment");
        mTypeScreen = TYPE_POPULAR_SONGS;
        initHeader();
    }

    public void openFavoriteSongsScreen() {
        replaceFragment(new FavoriteFragment(), "FavoriteFragment");
        mTypeScreen = TYPE_FAVORITE_SONGS;
        initHeader();
    }

    private void openFeedbackScreen() {
        replaceFragment(new FeedbackFragment(), "FeedbackFragment");
        mTypeScreen = TYPE_FEEDBACK;
        initHeader();
    }

    private void openContactScreen() {
        replaceFragment(new ContactFragment(), "ContactFragment");
        mTypeScreen = TYPE_CONTACT;
        initHeader();
    }
    private void openUpgradeScreen() {
        replaceFragment(new UpgradeFragment(), "UpgradeFragment");
        mTypeScreen = TYPE_UPGRADE;
        initHeader();
    }

    private void openChangePasswordScreen() {
        replaceFragment(new ChangePasswordFragment(), "ChangePasswordFragment");
        mTypeScreen = TYPE_CHANGE_PASSWORD;
        initHeader();
    }

    public void clickSeeAllCategory() {
        addFragment(CategoryFragment.newInstance(false), "CategoryFragment");
        handleToolbarTitle(getString(R.string.menu_category));
        handleDisplayIconHeader(false);
        handleDisplayButtonPlayAll(false);
    }

    public void clickSeeAllArtist() {
        addFragment(ArtistFragment.newInstance(false), "ArtistFragment");
        handleToolbarTitle(getString(R.string.menu_artist));
        handleDisplayIconHeader(false);
        handleDisplayButtonPlayAll(false);
    }

    public void clickSeeAllPopularSongs() {
        addFragment(new PopularSongsFragment(), "PopularSongsFragment");
        handleToolbarTitle(getString(R.string.menu_popular_songs));
        handleDisplayIconHeader(false);
        handleDisplayButtonPlayAll(true);
    }

    public void clickSeeAllFavoriteSongs() {
        addFragment(new FavoriteFragment(), "FavoriteFragment");
        handleToolbarTitle(getString(R.string.menu_favorite_songs));
        handleDisplayIconHeader(false);
        handleDisplayButtonPlayAll(true);
    }

    public void clickOpenSongsByCategory(Category category) {
        addFragment(SongsByCategoryFragment.newInstance(category.getId()), "SongsByCategoryFragment");
        handleToolbarTitle(category.getName());
        handleDisplayIconHeader(false);
        handleDisplayButtonPlayAll(true);
    }

    public void clickOpenSongsByArtist(Artist artist) {
        addFragment(SongsByArtistFragment.newInstance(artist.getId()), "SongsByArtistFragment");
        handleToolbarTitle(artist.getName());
        handleDisplayIconHeader(false);
        handleDisplayButtonPlayAll(true);
    }

    public void clickSearchSongScreen() {
        addFragment(new SearchFragment(), "SearchFragment");
        handleToolbarTitle(getString(R.string.label_search));
        handleDisplayIconHeader(false);
        handleDisplayButtonPlayAll(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.layout_close) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.layout_menu_home) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openHomeScreen();
        } else if (id == R.id.layout_menu_category) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openCategoryScreen();
        } else if (id == R.id.layout_menu_artist) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openArtistScreen();
        } else if (id == R.id.layout_menu_all_songs) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openAllSongsScreen();
        } else if (id == R.id.layout_menu_popular_songs) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openPopularSongsScreen();
        } else if (id == R.id.layout_menu_favorite_songs) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openFavoriteSongsScreen();
        } else if (id == R.id.layout_menu_feedback) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openFeedbackScreen();
        } else if (id == R.id.layout_menu_contact) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openContactScreen();
        } else if (id == R.id.layout_menu_upgrade) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openUpgradeScreen();
        } else if (id == R.id.layout_menu_change_password) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            openChangePasswordScreen();
        } else if (id == R.id.layout_menu_sign_out) {
            onClickSignOut();
        } else if (id == R.id.img_previous) {
            clickOnPrevButton();
        } else if (id == R.id.img_play) {
            clickOnPlayButton();
        } else if (id == R.id.img_next) {
            clickOnNextButton();
        } else if (id == R.id.img_close) {
            clickOnCloseButton();
        } else if (id == R.id.layout_text || id == R.id.img_song) {
            openPlayMusicActivity();
        }
    }

    public void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment, tag).commitAllowingStateLoss();
    }

    public void addFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_frame, fragment, tag)
//                .addToBackStack(fragment.getClass().getName())
                .addToBackStack(tag)
                .commit();
    }

    private void showConfirmExitApp() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive((dialog, which) -> finish())
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show();
    }

    private void displayLayoutBottom() {
        if (MusicService.mPlayer == null) {
            mActivityMainBinding.layoutBottom.layoutItem.setVisibility(View.GONE);
            return;
        }
        mActivityMainBinding.layoutBottom.layoutItem.setVisibility(View.VISIBLE);
        showInforSong();
        showStatusButtonPlay();
    }

    private void handleMusicAction() {
        if (Constant.CANNEL_NOTIFICATION == mAction) {
            mActivityMainBinding.layoutBottom.layoutItem.setVisibility(View.GONE);
            return;
        }
        mActivityMainBinding.layoutBottom.layoutItem.setVisibility(View.VISIBLE);
        showInforSong();
        showStatusButtonPlay();
    }

    private void showInforSong() {
        if (MusicService.mListSongPlaying == null || MusicService.mListSongPlaying.isEmpty()) {
            return;
        }
        Song currentSong = MusicService.mListSongPlaying.get(MusicService.mSongPosition);
        mActivityMainBinding.layoutBottom.tvSongName.setText(currentSong.getTitle());
        mActivityMainBinding.layoutBottom.tvArtist.setText(currentSong.getArtist());
        GlideUtils.loadUrl(currentSong.getImage(), mActivityMainBinding.layoutBottom.imgSong);
    }

    private void showStatusButtonPlay() {
        if (MusicService.isPlaying) {
            mActivityMainBinding.layoutBottom.imgPlay.setImageResource(R.drawable.ic_pause_black);
        } else {
            mActivityMainBinding.layoutBottom.imgPlay.setImageResource(R.drawable.ic_play_black);
        }
    }

    private void clickOnPrevButton() {
        GlobalFunction.startMusicService(this, Constant.PREVIOUS, MusicService.mSongPosition);
    }

    private void clickOnNextButton() {
        GlobalFunction.startMusicService(this, Constant.NEXT, MusicService.mSongPosition);
    }

    private void clickOnPlayButton() {
        if (MusicService.isPlaying) {
            GlobalFunction.startMusicService(this, Constant.PAUSE, MusicService.mSongPosition);
        } else {
            GlobalFunction.startMusicService(this, Constant.RESUME, MusicService.mSongPosition);
        }
    }

    private void clickOnCloseButton() {
        GlobalFunction.startMusicService(this, Constant.CANNEL_NOTIFICATION, MusicService.mSongPosition);
    }

    private void openPlayMusicActivity() {
        GlobalFunction.startActivity(this, PlayMusicActivity.class);
    }

    private void onClickSignOut() {
        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        // Stop service when user sign out
        clickOnCloseButton();
        GlobalFunction.startActivity(this, SignInActivity.class);
        finishAffinity();
    }

    public ActivityMainBinding getActivityMainBinding() {
        return mActivityMainBinding;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
            showConfirmExitApp();
        } else {
            GlobalFunction.hideSoftKeyboard(this);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        if (GlobalFunction.subscriptionListener != null) {
            GlobalFunction.subscriptionListener.remove();
        }
    }

    public void downloadSong(Song song) {
        mSong = song;
        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, REQUEST_PERMISSION_CODE);
            } else {
                GlobalFunction.startDownloadFile(this, mSong);
            }
        } else {
            GlobalFunction.startDownloadFile(this, mSong);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GlobalFunction.startDownloadFile(this, mSong);
            } else {
                Toast.makeText(this, getString(R.string.msg_permission_denied),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
        UpgradeFragment fragment = (UpgradeFragment) getSupportFragmentManager().findFragmentByTag("UpgradeFragment");
        if (fragment != null) {
            //Log.d("ZaloPay", "onNewIntent triggered!");
            fragment.handlePaymentResult(intent);
        }
    }
}
