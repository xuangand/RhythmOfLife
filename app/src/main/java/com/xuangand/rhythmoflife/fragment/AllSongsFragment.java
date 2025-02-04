package com.xuangand.rhythmoflife.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.xuangand.rhythmoflife.MyApplication;
import com.xuangand.rhythmoflife.R;
import com.xuangand.rhythmoflife.activity.MainActivity;
import com.xuangand.rhythmoflife.activity.PlayMusicActivity;
import com.xuangand.rhythmoflife.adapter.SongAdapter;
import com.xuangand.rhythmoflife.constant.Constant;
import com.xuangand.rhythmoflife.constant.GlobalFunction;
import com.xuangand.rhythmoflife.databinding.FragmentAllSongsBinding;
import com.xuangand.rhythmoflife.listener.IOnClickSongItemListener;
import com.xuangand.rhythmoflife.model.Song;
import com.xuangand.rhythmoflife.service.MusicService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllSongsFragment extends Fragment {

    private FragmentAllSongsBinding mFragmentAllSongsBinding;
    private List<Song> mListSong;
    private SongAdapter mSongAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentAllSongsBinding = FragmentAllSongsBinding.inflate(inflater, container, false);

        initUi();
        initListener();
        getListAllSongs();

        return mFragmentAllSongsBinding.getRoot();
    }

    private void initUi() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mFragmentAllSongsBinding.rcvData.setLayoutManager(linearLayoutManager);

        mListSong = new ArrayList<>();
        mSongAdapter = new SongAdapter(mListSong, new IOnClickSongItemListener() {
            @Override
            public void onClickItemSong(Song song) {
                goToSongDetail(song);
            }

            @Override
            public void onClickFavoriteSong(Song song, boolean favorite) {
                GlobalFunction.onClickFavoriteSong(getActivity(), song, favorite);
            }

            @Override
            public void onClickMoreOptions(Song song) {
                GlobalFunction.handleClickMoreOptions(getActivity(), song);
            }
        });
        mFragmentAllSongsBinding.rcvData.setAdapter(mSongAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getListAllSongs() {
        if (getActivity() == null) return;
        MyApplication.get(getActivity()).getSongsDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resetListData();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Song song = dataSnapshot.getValue(Song.class);
                    if (song == null) return;
                    mListSong.add(0, song);
                }
                if (mSongAdapter != null) mSongAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                GlobalFunction.showToastMessage(getActivity(), getString(R.string.msg_get_date_error));
            }
        });
    }

    private void resetListData() {
        if (mListSong == null) {
            mListSong = new ArrayList<>();
        } else {
            mListSong.clear();
        }
    }

    private void goToSongDetail(@NonNull Song song) {
        MusicService.clearListSongPlaying();
        MusicService.mListSongPlaying.add(song);
        MusicService.isPlaying = false;
        GlobalFunction.startMusicService(getActivity(), Constant.PLAY, 0);
        GlobalFunction.startActivity(getActivity(), PlayMusicActivity.class);
    }

    private void initListener() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null || activity.getActivityMainBinding() == null) {
            return;
        }
        activity.getActivityMainBinding().header.layoutPlayAll.setOnClickListener(v -> {
            if (mListSong == null || mListSong.isEmpty()) return;
            MusicService.clearListSongPlaying();
            MusicService.mListSongPlaying.addAll(mListSong);
            MusicService.isPlaying = false;
            GlobalFunction.startMusicService(getActivity(), Constant.PLAY, 0);
            GlobalFunction.startActivity(getActivity(), PlayMusicActivity.class);
        });
    }
}
