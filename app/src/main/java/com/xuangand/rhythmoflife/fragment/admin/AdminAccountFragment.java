package com.xuangand.rhythmoflife.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.xuangand.rhythmoflife.activity.AdminChangePasswordActivity;
import com.xuangand.rhythmoflife.activity.SignInActivity;
import com.xuangand.rhythmoflife.constant.GlobalFunction;
import com.xuangand.rhythmoflife.databinding.FragmentAdminAccountBinding;
import com.xuangand.rhythmoflife.prefs.DataStoreManager;

public class AdminAccountFragment extends Fragment {

    private FragmentAdminAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAccountBinding.inflate(inflater, container, false);

        initUi();

        return binding.getRoot();
    }

    private void initUi() {
        binding.tvEmail.setText(DataStoreManager.getUser().getEmail());
        binding.tvChangePassword.setOnClickListener(v -> onClickChangePassword());
        binding.tvSignOut.setOnClickListener(v -> onClickSignOut());
    }

    private void onClickChangePassword() {
        GlobalFunction.startActivity(getActivity(), AdminChangePasswordActivity.class);
    }

    private void onClickSignOut() {
        if (getActivity() == null) return;
        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(getActivity(), SignInActivity.class);
        getActivity().finishAffinity();
    }
}
