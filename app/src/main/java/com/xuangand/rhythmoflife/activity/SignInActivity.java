package com.xuangand.rhythmoflife.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.xuangand.rhythmoflife.R;
import com.xuangand.rhythmoflife.constant.Constant;
import com.xuangand.rhythmoflife.constant.GlobalFunction;
import com.xuangand.rhythmoflife.databinding.ActivitySignInBinding;
import com.xuangand.rhythmoflife.model.User;
import com.xuangand.rhythmoflife.prefs.DataStoreManager;
import com.xuangand.rhythmoflife.utils.StringUtil;

public class SignInActivity extends BaseActivity {

    private ActivitySignInBinding mActivitySignInBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySignInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(mActivitySignInBinding.getRoot());

        initListener();
    }

    private void initListener() {
        //mActivitySignInBinding.rdbUser.setChecked(true);
        mActivitySignInBinding.layoutSignUp.setOnClickListener(
                v -> GlobalFunction.startActivity(SignInActivity.this, SignUpActivity.class));

        mActivitySignInBinding.btnSignIn.setOnClickListener(v -> onClickValidateSignIn());
        mActivitySignInBinding.tvForgotPassword.setOnClickListener(
                v -> GlobalFunction.startActivity(this, ForgotPasswordActivity.class));
    }

//    private void onClickValidateSignIn() {
//        String strEmail = mActivitySignInBinding.edtEmail.getText().toString().trim();
//        String strPassword = mActivitySignInBinding.edtPassword.getText().toString().trim();
//        if (StringUtil.isEmpty(strEmail)) {
//            Toast.makeText(SignInActivity.this, getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show();
//        } else if (StringUtil.isEmpty(strPassword)) {
//            Toast.makeText(SignInActivity.this, getString(R.string.msg_password_require), Toast.LENGTH_SHORT).show();
//        } else if (!StringUtil.isValidEmail(strEmail)) {
//            Toast.makeText(SignInActivity.this, getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show();
//        } else {
//            if (mActivitySignInBinding.rdbAdmin.isChecked()) {
//                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
//                    Toast.makeText(SignInActivity.this, getString(R.string.msg_email_invalid_admin), Toast.LENGTH_SHORT).show();
//                } else {
//                    signInUser(strEmail, strPassword);
//                }
//                return;
//            }
//
//            if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
//                Toast.makeText(SignInActivity.this, getString(R.string.msg_email_invalid_user), Toast.LENGTH_SHORT).show();
//            } else {
//                signInUser(strEmail, strPassword);
//            }
//        }
//    }
    private void onClickValidateSignIn() {
        String strEmail = mActivitySignInBinding.edtEmail.getText().toString().trim();
        String strPassword = mActivitySignInBinding.edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            Toast.makeText(SignInActivity.this, getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show();
        } else if (StringUtil.isEmpty(strPassword)) {
            Toast.makeText(SignInActivity.this, getString(R.string.msg_password_require), Toast.LENGTH_SHORT).show();
        } else if (!StringUtil.isValidEmail(strEmail)) {
            Toast.makeText(SignInActivity.this, getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show();
        } else {
            signInUser(strEmail, strPassword);
        }
    }

    private void signInUser(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            User userObject = new User(user.getEmail(), password);
                            if (user.getEmail() != null && user.getEmail().contains(Constant.ADMIN_EMAIL_FORMAT)) {
                                userObject.setAdmin(true);
                            }
                            DataStoreManager.setUser(userObject);
                            goToMainActivity();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, getString(R.string.msg_sign_in_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMainActivity() {
        if (DataStoreManager.getUser().isAdmin()) {
            GlobalFunction.startActivity(SignInActivity.this, AdminMainActivity.class);
        } else {
            GlobalFunction.startActivity(SignInActivity.this, MainActivity.class);
        }
        finishAffinity();
    }
}