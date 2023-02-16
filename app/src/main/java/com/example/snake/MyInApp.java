package com.example.snake;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;
import static com.android.billingclient.api.BillingClient.SkuType.SUBS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.firebase.crashlytics.internal.model.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://programtown.com/how-to-make-in-app-purchase-in-android-using-google-play-billing-library/

public class MyInApp {

    private static MyInApp instance;
    private static Context appContext;

    public static MyInApp getInstance() {
        return instance;
    }

    public MyInApp(Context appContext, String LICENSE_KEY, Item[] items) {
        this.LICENSE_KEY = LICENSE_KEY;
        this.appContext = appContext.getApplicationContext();


        for (Item item : items) {
            newItem(item.type, item.PRODUCT_ID);
        }

        initSDK();
    }

    public static MyInApp initHelper(Context appContext, String LICENSE_KEY, Item[] items) {
        if (instance == null)
            instance = new MyInApp(appContext, LICENSE_KEY, items);
        return instance;
    }





    public enum TYPE {
        RepurchaseInApp,
        OneTimeInApp,
        Subscription
    }

    public interface CallBack_MyInApp {
        void successfullyPurchased(boolean isPurchasedNow, String purchaseKey);

        void purchaseFailed(String purchaseKey, int code, String message);

        void details(boolean isInAppExist, String title, String description, String price, long priceMic);
    }

    public static class Item {
        String PRODUCT_ID;
        TYPE type;
        ArrayList<CallBack_MyInApp> callBacks = new ArrayList<>();
        String SKU_TYPE;

        public Item() {}

        public Item(TYPE type, String PRODUCT_ID) {
            this.PRODUCT_ID = PRODUCT_ID;
            this.type = type;
        }
    }
    private HashMap<String, Item> items = new HashMap<>();

    private int initialStatus = -1; // -1 - no, 0 - without subscription, 1 - ok



    // prefs
    private static final String PREF_FILE = "MyPref";
    private static final String PURCHASE_KEY = "PURCHASE";

    private String LICENSE_KEY;
    //private AppCompatActivity appCompatActivity;

    private BillingClient billingClient;
    private boolean isToastActive = true;

    private void initialized(int status) {
        this.initialStatus = status;
    }

    public int getInitialStatus() {
        return initialStatus;
    }

    private void initSDK() {
        billingClient = BillingClient
                .newBuilder(appContext)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        initialized(0);
                        return;
                    }
                    initialized(1);

                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(INAPP).build(),
                            purchasesResponseListener1
                    );
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(SUBS).build(),
                            purchasesResponseListener2
                    );

                    billingClient.queryPurchaseHistoryAsync(
                            QueryPurchaseHistoryParams.newBuilder().setProductType(INAPP).build(),
                            purchaseHistoryResponseListener1
                    );
                    billingClient.queryPurchaseHistoryAsync(
                            QueryPurchaseHistoryParams.newBuilder().setProductType(SUBS).build(),
                            purchaseHistoryResponseListener2
                    );

                } else {
                    initialized(-1);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                int x = 0;
                int y = x + 3;
            }
        });
    }

    public void newItem(TYPE type, String PRODUCT_ID) {
        if (items.containsKey(PRODUCT_ID)) {
            return;
        }

        Item item = new Item();
        item.type = type;
        item.PRODUCT_ID = PRODUCT_ID;

        if (type == TYPE.RepurchaseInApp || type == TYPE.OneTimeInApp) {
            item.SKU_TYPE = INAPP;
        } else if (type == TYPE.Subscription) {
            item.SKU_TYPE = SUBS;
        }

        items.put(PRODUCT_ID, item);
    }

    public void addCallBack(String _PRODUCT_ID, CallBack_MyInApp _callBack_myInApp) {
        Item item = items.get(_PRODUCT_ID);
        if (item == null) {
            return;
        }

        item.callBacks.add(_callBack_myInApp);
    }

    public void removeCallBack(String _PRODUCT_ID, CallBack_MyInApp _callBack_myInApp) {
        Item item = items.get(_PRODUCT_ID);
        if (item == null) {
            return;
        }

        item.callBacks.remove(_callBack_myInApp);
    }

    public boolean consume(AppCompatActivity appCompatActivity, String _PRODUCT_ID) {
        Item item = items.get(_PRODUCT_ID);
        if (item == null) {
            return false;
        }

        //check if service is already connected
        if (billingClient.isReady()) {
            initiatePurchase(appCompatActivity, item);
            return true;
        } else {
            //else reconnect service
            initSDK();
            return false;
        }
    }

    public boolean getInAppDetails(String _PRODUCT_ID) {
        Log.d("ptttSub", "getInAppDetails(" + _PRODUCT_ID + ") ");
        Item item = items.get(_PRODUCT_ID);
        if (item == null) {
            return false;
        }

        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.from(
                QueryProductDetailsParams.Product
                        .newBuilder()
                        .setProductId(item.PRODUCT_ID)
                        .setProductType(item.SKU_TYPE)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams
                .newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(
                params,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (productDetailsList != null && productDetailsList.size() > 0) {
                                ProductDetails productDetails = productDetailsList.get(0);
                                String title = productDetails.getTitle();
                                String description = productDetails.getDescription();

                                String price = "-0.00";
                                long priceMic = 0;
                                if (item.SKU_TYPE == INAPP) {
                                    price = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                                    priceMic = productDetails.getOneTimePurchaseOfferDetails().getPriceAmountMicros();
                                } else if (item.SKU_TYPE == SUBS) {
                                    price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                    priceMic = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros();
                                }

                                for (CallBack_MyInApp callBack : item.callBacks) {
                                    if (callBack != null) {
                                        String finalPrice = price;
                                        long finalPriceMic = priceMic;
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            try {
                                                callBack.details(true, title, description, finalPrice, finalPriceMic);
                                            } catch (IllegalStateException ex) {}
                                        });
                                    }
                                }

                            } else {
                                for (CallBack_MyInApp callBack : item.callBacks) {
                                    if (callBack != null) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            try {
                                                callBack.details(false, "", "", "", 0);
                                            } catch (IllegalStateException ex) {}
                                        });
                                    }
                                }
                            }
                        } else {
                            for (CallBack_MyInApp callBack : item.callBacks) {
                                if (callBack != null) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        try {
                                            callBack.details(false, "", "", "", 0);
                                        } catch (IllegalStateException ex) {}
                                    });
                                }
                            }
                        }
                    }
                }
        );

        return true;
    }

    private void initiatePurchase(AppCompatActivity appCompatActivity, Item item) {
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.from(
                QueryProductDetailsParams.Product
                        .newBuilder()
                        .setProductId(item.PRODUCT_ID)
                        .setProductType(item.SKU_TYPE)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams
                .newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(
                params,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (productDetailsList != null && productDetailsList.size() > 0) {
                                // Retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                // Get the offerToken of the selected offer

                                ProductDetails productDetails = productDetailsList.get(0);
                                ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = null;

                                if (item.type == TYPE.RepurchaseInApp || item.type == TYPE.OneTimeInApp) {
                                    // Set the parameters for the offer that will be presented
                                    // in the billing flow creating separate productDetailsParamsList variable
                                    productDetailsParamsList =
                                            ImmutableList.from(
                                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                                            .setProductDetails(productDetails)
                                                            .build()
                                            );
                                } else if (item.type == TYPE.Subscription) {

                                    String offerToken = productDetails
                                            .getSubscriptionOfferDetails()
                                            .get(0)
                                            .getOfferToken();

                                    // Set the parameters for the offer that will be presented
                                    // in the billing flow creating separate productDetailsParamsList variable
                                    productDetailsParamsList =
                                            ImmutableList.from(
                                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                                            .setProductDetails(productDetails)
                                                            .setOfferToken(offerToken)
                                                            .build()
                                            );
                                }






                                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(productDetailsParamsList)
                                        .build();

                                // Launch the billing flow
                                BillingResult billingResult2 = billingClient.launchBillingFlow(appCompatActivity, billingFlowParams);

                            } else {
                                //try to add item/product id "consumable" inside managed product in google play console
                                failed(item, 1);
                            }
                        } else {
                            failed(item, "Error " + billingResult.getDebugMessage());
                        }
                    }
                }
        );
    }

    void handlePurchases(List<Purchase> purchases) {

        for (Purchase purchase : purchases) {
            List<String> x = purchase.getProducts();
            Item item = items.get(purchase.getProducts().get(0));

            if (item == null) {
                failed(item, 1);
                continue;
            }

            //if item is purchased
            if (item.PRODUCT_ID.equals(purchase.getProducts().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    // Invalid purchase
                    // show error to user
                    failed(item, 2);
                    return;
                }
                // else purchase is valid
                //if item is purchased and not acknowledged

                if (!purchase.isAcknowledged()) {
                    if (item.type == TYPE.RepurchaseInApp) {
                        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                                Log.d("pttt", "onConsumeResponse");
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    savePurchaseValueToPref(item.PRODUCT_ID, true);
                                    success(item, true);
                                }
                            }
                        });
                    } else if (item.type == TYPE.OneTimeInApp || item.type == TYPE.Subscription) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                            @Override
                            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    //if purchase is acknowledged
                                    // Grant entitlement to the user. and restart activity
                                    savePurchaseValueToPref(item.PRODUCT_ID, true);
                                    success(item, true);
                                    // recreate your activity
                                }
                            }
                        });
                    }
                }

                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if (item.type == TYPE.OneTimeInApp || item.type == TYPE.Subscription) {
                        if (!getPurchaseValueFromPref(item.PRODUCT_ID)) {
                            savePurchaseValueToPref(item.PRODUCT_ID, true);
                        }
                        success(item, false);
                    }
                }
            }
            //if purchase is pending
            else if (item.PRODUCT_ID.equals(purchase.getProducts().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                failed(item, 3);
            }
            //if purchase is refunded or unknown
            else if (item.PRODUCT_ID.equals(purchase.getProducts().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                if (item.type == TYPE.OneTimeInApp || item.type == TYPE.Subscription) {
                    savePurchaseValueToPref(item.PRODUCT_ID, false);
                }
                failed(item, 4);
            }
        }
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            //for old playconsole
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            //for new play console
            //To get key go to Developer Console > Select your app > Monetize > Monetization setup

            String base64Key = LICENSE_KEY;
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
            //if item newly purchased
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                handlePurchases(purchases);
            }
            //if item already purchased then check and reflect changes
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                if (purchases == null) {
                    failed(null, 5);
                    return;
                }

                Item item = items.get(purchases.get(0).getProducts().get(0));
                if (item.type == TYPE.RepurchaseInApp) {
                    forceConsumingPurchase(item);
                } else {
                    failed(item, 5);
                }
            }
            //if purchase cancelled
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                if (purchases == null) {
                    failed(null, 6);
                    return;
                }

                Item item = items.get(purchases.get(0).getProducts().get(0));
                failed(item, 6);
            }
            // Handle any other error msgs
            else {
                Item item = null;
                try {
                    item = items.get(purchases.get(0).getProducts().get(0));
                } catch (NullPointerException e) {

                }
                failed(item, "Error " + billingResult.getDebugMessage());
            }
        }
    };

    private PurchaseHistoryResponseListener purchaseHistoryResponseListener1 = new PurchaseHistoryResponseListener() {
        @Override
        public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
            for (PurchaseHistoryRecord purchaseHistoryRecord : list) {
                Log.d("pttt", purchaseHistoryRecord.getProducts().get(0));
                List<String> lst = purchaseHistoryRecord.getProducts();
                int x = purchaseHistoryRecord.getQuantity();
                int z = 0;
            }
        }
    };
    private PurchaseHistoryResponseListener purchaseHistoryResponseListener2 = new PurchaseHistoryResponseListener() {
        @Override
        public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
            for (PurchaseHistoryRecord purchaseHistoryRecord : list) {
                Log.d("pttt", purchaseHistoryRecord.getProducts().get(0));
                List<String> lst = purchaseHistoryRecord.getProducts();
                int x = purchaseHistoryRecord.getQuantity();
                int z = 0;
            }
        }
    };

    private PurchasesResponseListener purchasesResponseListener1 = new PurchasesResponseListener() {
        @Override
        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
            Log.d("pttt", "onQueryPurchasesResponse");
            handlePurchases(list);

            if (list == null || list.size() == 0) {
                for (Map.Entry<String, Item> entry : items.entrySet()) {
                    Item item = entry.getValue();
                    if (item.type == TYPE.RepurchaseInApp) {
                        savePurchaseValueToPref(item.PRODUCT_ID, false);
                    }
                }
            }
        }
    };
    private PurchasesResponseListener purchasesResponseListener2 = new PurchasesResponseListener() {
        @Override
        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
            Log.d("pttt", "onQueryPurchasesResponse");
            handlePurchases(list);

            if (list == null || list.size() == 0) {
                for (Map.Entry<String, Item> entry : items.entrySet()) {
                    Item item = entry.getValue();
                    if (item.type == TYPE.Subscription) {
                        savePurchaseValueToPref(item.PRODUCT_ID, false);
                    }
                }
            }
        }
    };

    private void forceConsumingPurchase(Item item) {
        // if purchased with old billing library or acknowledgePurchase(one time purchase) instead of consume purchase
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(item.SKU_TYPE).build(),
                new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                        for (Purchase purchase : purchases) {
                            //if item is purchased
                            if (item.PRODUCT_ID.equals(purchase.getProducts().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                                    return;
                                }

                                ConsumeParams consumeParams = ConsumeParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();

                                billingClient.consumeAsync(consumeParams, null);
                            }
                        }
                    }
                });
    }

    private void success(Item item, boolean isPurchasedNow) {
        for (CallBack_MyInApp callBack : item.callBacks) {
            if (callBack != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callBack.successfullyPurchased(isPurchasedNow, item.PRODUCT_ID);
                    } catch (IllegalStateException ex) {}
                });
            }
        }

        if (isToastActive) {
            //toast("Item Consumed");
        }
    }

    private void failed(Item item, String message) {
        failed(item, 0, message);
    }

    private void failed(Item item, int code) {
        HashMap<Integer, String> failures = new HashMap<>();
        failures.put(1, "Purchase Item not Found");
        failures.put(2, "Error : Invalid Purchase");
        failures.put(3, "Purchase is Pending. Please complete Transaction");
        failures.put(4, "Purchase Status Unknown");
        failures.put(5, "Item already owned");
        failures.put(6, "Purchase Canceled");

        failed(item, code, failures.get(code));
    }

    private void failed(Item item, int code, String message) {
        if (item != null) {
            for (CallBack_MyInApp callBack : item.callBacks) {
                if (callBack != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            callBack.purchaseFailed(item.PRODUCT_ID, code, message);
                        } catch (IllegalStateException ex) {}
                    });
                }
            }
        }

        if (isToastActive) {
            toast(message);
        }
    }

    public void toast(final String message) {
        // If we put it into handler - we can call in from asynctask outside of main uithread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                } catch (IllegalStateException ex) {}
            }
        });
    }

    public void destroy() {
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

    private SharedPreferences getPreferenceObject() {
        return appContext.getSharedPreferences(PREF_FILE, 0);
    }

    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = appContext.getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }

    public boolean getPurchaseValueFromPref(String PRODUCT_ID) {
        return getPreferenceObject().getBoolean(PURCHASE_KEY + "_" + PRODUCT_ID, false);
    }

    private void savePurchaseValueToPref(String PRODUCT_ID, boolean value) {
        Log.d("pttts", "savePurchaseValueToPref() PRODUCT_ID=" + PRODUCT_ID);
        getPreferenceEditObject().putBoolean(PURCHASE_KEY + "_" + PRODUCT_ID, value).commit();
    }

    public static boolean getPurchaseValueFromPref(Context context, String PRODUCT_ID) {
        SharedPreferences pref = context.getSharedPreferences(PREF_FILE, 0);
        return pref.getBoolean(PURCHASE_KEY + "_" + PRODUCT_ID, false);
    }
}
