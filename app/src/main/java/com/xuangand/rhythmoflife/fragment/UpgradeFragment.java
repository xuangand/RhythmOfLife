package com.xuangand.rhythmoflife.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xuangand.rhythmoflife.Api.CreateOrder;
import com.xuangand.rhythmoflife.R;
import com.xuangand.rhythmoflife.activity.MainActivity;
import com.xuangand.rhythmoflife.constant.UpgradeInfo;
import com.xuangand.rhythmoflife.constant.ZaloPayInfo;
import com.xuangand.rhythmoflife.databinding.FragmentUpgradeBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class UpgradeFragment extends Fragment {

    private FragmentUpgradeBinding mFragmentUpgradeBinding;

    public UpgradeFragment() {
        // Required empty public constructor
    }

    private void initUi() {
        mFragmentUpgradeBinding.tvUpgradeTitle.setText(UpgradeInfo.UPGRADE_TITLE);
        mFragmentUpgradeBinding.tvUpgradeContent.setText(UpgradeInfo.UPGRADE_CONTENT);
    }
    private void initListener() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(ZaloPayInfo.APP_ID, Environment.SANDBOX);


        String total =  mFragmentUpgradeBinding.priceText.getText().toString().replace("₫", "").replace(".", "").replace(",", "");
        Double totalDouble = Double.parseDouble(total);
        String totalString = String.format("%.0f", totalDouble);

        mFragmentUpgradeBinding.purchaseButton.setOnClickListener( view -> {
            CreateOrder orderApi = new CreateOrder();
            try {
                JSONObject data = orderApi.createOrder(totalString);
                String code = data.getString("return_code");

                if (code.equals("1")) {
                    String token = data.getString("zp_trans_token");
                    Activity activity = getActivity();
                    if (activity == null) {
                        Log.e("ZaloPay", "Activity is null, cannot proceed with payment.");
                        return;
                    }
                    ZaloPaySDK.getInstance().payOrder(getActivity(), token, "demozpdk://app", new PayOrderListener(){
                        @Override
                        public void onPaymentSucceeded(String s, String s1, String s2) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseUser user = auth.getCurrentUser();
                            //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get logged-in user ID

                            if (user != null) {
                                String userId = user.getUid(); // This is the unique ID for the user

                                // Create payment history object
                                Map<String, Object> paymentData = new HashMap<>();
                                paymentData.put("user_id", userId);
                                paymentData.put("transaction_token", s1);
                                paymentData.put("amount", Double.parseDouble(totalString));
                                paymentData.put("date", System.currentTimeMillis()); // Store timestamp
                                paymentData.put("isPro", true);

                                // Save to Firestore
                                db.collection("PurchasedHistory")
                                        .add(paymentData)
                                        .addOnSuccessListener(documentReference -> {
                                            //Log.d("Firestore", "Payment saved with ID: " + documentReference.getId());
                                            Toast.makeText(getContext(), "Payment successful", Toast.LENGTH_SHORT).show();
                                            if (getActivity() instanceof MainActivity) {
                                                ((MainActivity)getActivity()).replaceFragment(new HomeFragment(), "HomeFragment");
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error saving payment", e));

                            }else {
                                Log.e("Firestore", "User not authenticated");
                            }
                        }

                        @Override
                        public void onPaymentCanceled(String s, String s1) {
                            Toast.makeText(getContext(), "Payment canceled", Toast.LENGTH_SHORT).show();
                            Log.d("ZaloPay", "Payment Canceled");
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                            Toast.makeText(getContext(), "Payment error", Toast.LENGTH_SHORT).show();
                            Log.e("ZaloPay", "Payment failed");
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentUpgradeBinding = FragmentUpgradeBinding.inflate(inflater, container, false);
        initUi();
        initListener();
        return mFragmentUpgradeBinding.getRoot();
    }
    public void handlePaymentResult(Intent intent) {
        ZaloPaySDK.getInstance().onResult(intent);
    }

}