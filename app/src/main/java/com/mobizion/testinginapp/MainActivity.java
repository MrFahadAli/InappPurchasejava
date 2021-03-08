package com.mobizion.testinginapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    Button buy_btn,btn;
    TextView tv;
    EditText edt;
    BillingClient billingClient;
    List<String> skulist = new ArrayList<>();
    String product = "app_purchase_local";

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseCrashlytics FirebaseCrashlytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buy_btn = findViewById(R.id.buy_button_1);
        tv = findViewById(R.id.tv);
       // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);




        //example
//        Button crashButton = new Button(this);
//        crashButton.setText("Crash!");
//        crashButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                throw new RuntimeException("Test Crash"); // Force a crash
//            }
//        });

//
//        addContentView(crashButton, new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));



        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        //Bundle bundle = new Bundle();
        //bundle.putString(FirebaseAnalytics.Param.ITEM_ID, buy_btn);
        //bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
      //  bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
      ///  mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);



        billingClient = BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            //This method starts when user buys a product
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
                if(list != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                {
                    for(Purchase purchase : list)
                    {
                        handlepurchase(purchase);
                    }
                }
                else
                {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED)
                    {
                        Log.v("onPurchases If ","ON Perchse update Chal prha ha ");

                        Toast.makeText(MainActivity.this, "Try Purchasing Again", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Log.v("onPurchases else If ","ON Perchse update Chal prha ha ");

                        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED)
                        {
                            Toast.makeText(MainActivity.this, "Already Purchased", Toast.LENGTH_SHORT).show();
                            //We recover that method by consuming a purchase so that user can buy a product again from same account.
                        }
                    }
                }
            }
        }).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.v("onBillingSetupFinished","ON Biiling function Chal prha ha ");

                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                {

                    Log.v("onBillingSetupFinished","If ON Biiling function Chal prha ha ");
                    Toast.makeText(MainActivity.this, "Successfully connected to Billing client", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(MainActivity.this, "Disconnected from the Client", Toast.LENGTH_SHORT).show();
            }
        });
        skulist.add(product);
        final SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skulist).setType(BillingClient.SkuType.INAPP);  //Skutype.subs for Subscription
        buy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("Buttoon","Button click");
                billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {

                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                        Log.v("Buttoon2","Button click");

                        if(list != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                        {
                            Log.v("Buttoon4","Button click");

                            for(final SkuDetails skuDetails : list)
                            {

                                String sku = skuDetails.getSku(); // your Product id
                                String price = skuDetails.getPrice(); // your product price
                                String description = skuDetails.getDescription(); // product description
                                //method opens Popup for billing purchase
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetails)
                                        .build();
                                BillingResult responsecode = billingClient.launchBillingFlow(MainActivity.this,flowParams);
                            }
                        }
                    }
                });
            }
        });
    }
    private void handlepurchase(Purchase purchase) {
        try {

            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                if (purchase.getSku().equals(product)) {
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                    ConsumeResponseListener consumeResponseListener = new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(BillingResult billingResult, String s) {
                            Toast.makeText(MainActivity.this, "Purchase Acknowledged", Toast.LENGTH_SHORT).show();
                        }
                    };
                    billingClient.consumeAsync(consumeParams, consumeResponseListener);
                    //now you can purchase same product again and again
                    //Here we give coins to user.
                    tv.setText("Purchase Successful");
                    Toast.makeText(MainActivity.this, "Purchase Successful", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        Toast.makeText(MainActivity.this, "onPurchases Updated", Toast.LENGTH_SHORT).show();
    }
}