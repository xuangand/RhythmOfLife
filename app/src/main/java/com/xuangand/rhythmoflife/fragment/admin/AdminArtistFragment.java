package com.xuangand.rhythmoflife.fragment.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.xuangand.rhythmoflife.MyApplication;
import com.xuangand.rhythmoflife.R;
import com.xuangand.rhythmoflife.activity.AdminAddArtistActivity;
import com.xuangand.rhythmoflife.activity.AdminArtistSongActivity;
import com.xuangand.rhythmoflife.adapter.AdminArtistAdapter;
import com.xuangand.rhythmoflife.constant.Constant;
import com.xuangand.rhythmoflife.constant.GlobalFunction;
import com.xuangand.rhythmoflife.databinding.FragmentAdminArtistBinding;
import com.xuangand.rhythmoflife.listener.IOnAdminManagerArtistListener;
import com.xuangand.rhythmoflife.model.Artist;
import com.xuangand.rhythmoflife.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminArtistFragment extends Fragment {

    private FragmentAdminArtistBinding binding;
    private List<Artist> mListArtist;
    private AdminArtistAdapter mAdminArtistAdapter;
    private ChildEventListener mChildEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminArtistBinding.inflate(inflater, container, false);
        initView();
        initListener();
        loadListArtist("");


        return binding.getRoot();
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rcvArtist.setLayoutManager(linearLayoutManager);
        mListArtist = new ArrayList<>();
        mAdminArtistAdapter = new AdminArtistAdapter(mListArtist, new IOnAdminManagerArtistListener() {
            @Override
            public void onClickUpdateArtist(Artist artist) {
                onClickEditArtist(artist);
            }

            @Override
            public void onClickDeleteArtist(Artist artist) {
                deleteArtistItem(artist);
            }

            @Override
            public void onClickDetailArtist(Artist artist) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constant.KEY_INTENT_ARTIST_OBJECT, artist);
                GlobalFunction.startActivity(getActivity(), AdminArtistSongActivity.class, bundle);
            }
        });
        binding.rcvArtist.setAdapter(mAdminArtistAdapter);
        binding.rcvArtist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    binding.btnAddArtist.hide();
                } else {
                    binding.btnAddArtist.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initListener() {
        binding.btnAddArtist.setOnClickListener(v -> onClickAddArtist());

        binding.imgSearch.setOnClickListener(view1 -> searchArtist());

        binding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchArtist();
                return true;
            }
            return false;
        });

        binding.edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKey = s.toString().trim();
                if (strKey.equals("") || strKey.length() == 0) {
                    searchArtist();
                }
            }
        });
    }

    private void onClickAddArtist() {
        GlobalFunction.startActivity(getActivity(), AdminAddArtistActivity.class);
    }

    private void onClickEditArtist(Artist artist) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.KEY_INTENT_ARTIST_OBJECT, artist);
        GlobalFunction.startActivity(getActivity(), AdminAddArtistActivity.class, bundle);
    }

    private void deleteArtistItem(Artist artist) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> {
                    if (getActivity() == null) return;
                    MyApplication.get(getActivity()).getArtistDatabaseReference()
                            .child(String.valueOf(artist.getId())).removeValue((error, ref) ->
                                    Toast.makeText(getActivity(),
                                            getString(R.string.msg_delete_artist_successfully),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void searchArtist() {
        String strKey = binding.edtSearchName.getText().toString().trim();
        resetListArtist();
        if (getActivity() != null) {
            MyApplication.get(getActivity()).getArtistDatabaseReference()
                    .removeEventListener(mChildEventListener);
        }
        loadListArtist(strKey);
        GlobalFunction.hideSoftKeyboard(getActivity());
    }

    private void resetListArtist() {
        if (mListArtist != null) {
            mListArtist.clear();
        } else {
            mListArtist = new ArrayList<>();
        }
    }

    public void loadListArtist(String keyword) {
        if (getActivity() == null) return;
        mChildEventListener = new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Artist artist = dataSnapshot.getValue(Artist.class);
                if (artist == null || mListArtist == null) return;
                if (StringUtil.isEmpty(keyword)) {
                    mListArtist.add(0, artist);
                } else {
                    if (GlobalFunction.getTextSearch(artist.getName()).toLowerCase().trim()
                            .contains(GlobalFunction.getTextSearch(keyword).toLowerCase().trim())) {
                        mListArtist.add(0, artist);
                    }
                }
                if (mAdminArtistAdapter != null) mAdminArtistAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Artist artist = dataSnapshot.getValue(Artist.class);
                if (artist == null || mListArtist == null || mListArtist.isEmpty()) return;
                for (int i = 0; i < mListArtist.size(); i++) {
                    if (artist.getId() == mListArtist.get(i).getId()) {
                        mListArtist.set(i, artist);
                        break;
                    }
                }
                if (mAdminArtistAdapter != null) mAdminArtistAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Artist artist = dataSnapshot.getValue(Artist.class);
                if (artist == null || mListArtist == null || mListArtist.isEmpty()) return;
                for (Artist artistObject : mListArtist) {
                    if (artist.getId() == artistObject.getId()) {
                        mListArtist.remove(artistObject);
                        break;
                    }
                }
                if (mAdminArtistAdapter != null) mAdminArtistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        MyApplication.get(getActivity()).getArtistDatabaseReference()
                .addChildEventListener(mChildEventListener);
    }
}
