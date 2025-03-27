package com.xuangand.rhythmoflife.constant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.xuangand.rhythmoflife.MyApplication;
import com.xuangand.rhythmoflife.R;
import com.xuangand.rhythmoflife.activity.MainActivity;
import com.xuangand.rhythmoflife.activity.PlayMusicActivity;
import com.xuangand.rhythmoflife.databinding.LayoutBottomSheetOptionBinding;
import com.xuangand.rhythmoflife.model.Song;
import com.xuangand.rhythmoflife.model.UserInfor;
import com.xuangand.rhythmoflife.prefs.DataStoreManager;
import com.xuangand.rhythmoflife.service.MusicReceiver;
import com.xuangand.rhythmoflife.service.MusicService;
import com.xuangand.rhythmoflife.utils.GlideUtils;
import com.xuangand.rhythmoflife.utils.StringUtil;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GlobalFunction {

    public static void startActivity(Context context, Class<?> clz) {
        Intent intent = new Intent(context, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(context, clz);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.
                    getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

//    public static void onClickOpenGmail(Context context) {
//        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                "mailto", AboutUsConfig.GMAIL, null));
//        context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
//    }

//    public static void onClickOpenSkype(Context context) {
//        try {
//            Uri skypeUri = Uri.parse("skype:" + AboutUsConfig.SKYPE_ID + "?chat");
//            context.getPackageManager().getPackageInfo("com.skype.raider", 0);
//            Intent skypeIntent = new Intent(Intent.ACTION_VIEW, skypeUri);
//            skypeIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
//            context.startActivity(skypeIntent);
//        } catch (Exception e) {
//            openSkypeWebview(context);
//        }
//    }

//    private static void openSkypeWebview(Context context) {
//        try {
//            context.startActivity(new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("skype:" + AboutUsConfig.SKYPE_ID + "?chat")));
//        } catch (Exception exception) {
//            String skypePackageName = "com.skype.raider";
//            try {
//                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + skypePackageName)));
//            } catch (android.content.ActivityNotFoundException anfe) {
//                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + skypePackageName)));
//            }
//        }
//    }

    public static void onClickOpenFacebook(Context context) {
        Intent intent;
        try {
            String urlFacebook = AboutUsConfig.PAGE_FACEBOOK;
            PackageManager packageManager = context.getPackageManager();
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                urlFacebook = "fb://facewebmodal/f?href=" + AboutUsConfig.LINK_FACEBOOK;
            }
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlFacebook));
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.LINK_FACEBOOK));
        }
        context.startActivity(intent);
    }

    public static void onClickOpenInstagram(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.LINK_INSTAGRAM)));
    }

//    public static void callPhoneNumber(Activity activity) {
//        try {
//            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 101);
//                return;
//            }
//
//            Intent callIntent = new Intent(Intent.ACTION_CALL);
//            callIntent.setData(Uri.parse("tel:" + AboutUsConfig.PHONE_NUMBER));
//            activity.startActivity(callIntent);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getTextSearch(String input) {
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static void startMusicService(Context ctx, int action, int songPosition) {
        Intent musicService = new Intent(ctx, MusicService.class);
        musicService.putExtra(Constant.MUSIC_ACTION, action);
        musicService.putExtra(Constant.SONG_POSITION, songPosition);
        ctx.startService(musicService);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static PendingIntent openMusicReceiver(Context ctx, int action) {
        Intent intent = new Intent(ctx, MusicReceiver.class);
        intent.putExtra(Constant.MUSIC_ACTION, action);
        int pendingFlag = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        return PendingIntent.getBroadcast(ctx.getApplicationContext(), action, intent, pendingFlag);
    }

    public static boolean isFavoriteSong(Song song) {
        if (song.getFavorite() == null || song.getFavorite().isEmpty()) return false;
        List<UserInfor> listUsersFavorite = new ArrayList<>(song.getFavorite().values());
        if (listUsersFavorite.isEmpty()) return false;
        for (UserInfor userInfor : listUsersFavorite) {
            if (DataStoreManager.getUser().getEmail().equals(userInfor.getEmailUser())) {
                return true;
            }
        }
        return false;
    }

    public static UserInfor getUserFavoriteSong(Song song) {
        UserInfor userInfor = null;
        if (song.getFavorite() == null || song.getFavorite().isEmpty()) return null;
        List<UserInfor> listUsersFavorite = new ArrayList<>(song.getFavorite().values());
        if (listUsersFavorite.isEmpty()) return null;
        for (UserInfor userObject : listUsersFavorite) {
            if (DataStoreManager.getUser().getEmail().equals(userObject.getEmailUser())) {
                userInfor = userObject;
                break;
            }
        }
        return userInfor;
    }

    public static void onClickFavoriteSong(Context context, Song song, boolean isFavorite) {
        if (context == null) return;
        if (isFavorite) {
            String userEmail = DataStoreManager.getUser().getEmail();
            UserInfor userInfor = new UserInfor(System.currentTimeMillis(), userEmail);
            MyApplication.get(context).getSongsDatabaseReference()
                    .child(String.valueOf(song.getId()))
                    .child("favorite")
                    .child(String.valueOf(userInfor.getId()))
                    .setValue(userInfor);
        } else {
            UserInfor userInfor = getUserFavoriteSong(song);
            if (userInfor != null) {
                MyApplication.get(context).getSongsDatabaseReference()
                        .child(String.valueOf(song.getId()))
                        .child("favorite")
                        .child(String.valueOf(userInfor.getId()))
                        .removeValue();
            }
        }
    }

    @SuppressLint("InflateParams")
    public static void handleClickMoreOptions(Activity context, Song song) {
        if (context == null || song == null) return;

        LayoutBottomSheetOptionBinding binding = LayoutBottomSheetOptionBinding
                .inflate(LayoutInflater.from(context));

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        GlideUtils.loadUrl(song.getImage(), binding.imgSong);
        binding.tvSongName.setText(song.getTitle());
        binding.tvArtist.setText(song.getArtist());

        if (MusicService.isSongExist(song.getId())) {
            binding.layoutRemovePlaylist.setVisibility(View.VISIBLE);
            binding.layoutPriority.setVisibility(View.VISIBLE);
            binding.layoutAddPlaylist.setVisibility(View.GONE);
        } else {
            binding.layoutRemovePlaylist.setVisibility(View.GONE);
            binding.layoutPriority.setVisibility(View.GONE);
            binding.layoutAddPlaylist.setVisibility(View.VISIBLE);
        }

        binding.layoutDownload.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.downloadSong(song);
            bottomSheetDialog.hide();
        });

        binding.layoutPriority.setOnClickListener(view -> {
            if (MusicService.isSongPlaying(song.getId())) {
                showToastMessage(context, context.getString(R.string.msg_song_playing));
            } else {
                for (Song songEntity : MusicService.mListSongPlaying) {
                    songEntity.setPriority(songEntity.getId() == song.getId());
                }
                showToastMessage(context, context.getString(R.string.msg_setting_priority_successfully));
            }
            bottomSheetDialog.hide();
        });

        binding.layoutAddPlaylist.setOnClickListener(view -> {
            if (MusicService.mListSongPlaying == null || MusicService.mListSongPlaying.isEmpty()) {
                MusicService.clearListSongPlaying();
                MusicService.mListSongPlaying.add(song);
                MusicService.isPlaying = false;
                GlobalFunction.startMusicService(context, Constant.PLAY, 0);
                GlobalFunction.startActivity(context, PlayMusicActivity.class);
            } else {
                MusicService.mListSongPlaying.add(song);
                showToastMessage(context, context.getString(R.string.msg_add_song_playlist_success));
            }
            bottomSheetDialog.hide();
        });

        binding.layoutRemovePlaylist.setOnClickListener(view -> {
            if (MusicService.isSongPlaying(song.getId())) {
                showToastMessage(context, context.getString(R.string.msg_cannot_delete_song));
            } else {
                MusicService.deleteSongFromPlaylist(song.getId());
                showToastMessage(context, context.getString(R.string.msg_delete_song_from_playlist_success));
            }
            bottomSheetDialog.hide();
        });

        bottomSheetDialog.show();
    }

    public static void startDownloadFile(Activity activity, Song song) {
        if (activity == null || song == null || StringUtil.isEmpty(song.getUrl())) return;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(song.getUrl()));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(activity.getString(R.string.title_download));
        request.setDescription(activity.getString(R.string.message_download));

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String fileName = song.getTitle() + ".mp3";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }
    public static ListenerRegistration subscriptionListener;
    public static void checkSubscription(OnSubscriptionCheckListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        long oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 days ago
        if (user == null) {
            listener.onResult(false); // User not logged in
            return;
        }
        String userId = user.getUid();
        Query query = db.collection("PurchasedHistory")
                .whereEqualTo("user_id", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1);

        subscriptionListener = query.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error checking subscription", error);
                listener.onResult(false);
                return;
            }

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot latestPayment = queryDocumentSnapshots.getDocuments().get(0);
                Long paymentDate = latestPayment.getLong("date");
                String transactionToken = latestPayment.getString("transaction_token");

                // Check if the latest payment is within the last 30 days and has a valid transaction token
                listener.onResult(paymentDate != null && paymentDate > oneMonthAgo &&
                        transactionToken != null && !transactionToken.isEmpty());  // Subscription is active/expired
            } else {
                listener.onResult(false); // No payment history found
            }
        });
    }
    public interface OnSubscriptionCheckListener {
        void onResult(boolean isActive);
    }
}