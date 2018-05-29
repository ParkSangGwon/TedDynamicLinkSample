package gun0912.dynamiclink.sample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ted";

    private static final String SEGMENT_PROMOTION = "promotion";
    private static final String KEY_CODE = "code";

    private static final int REQ_CODE_INVITE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_share_dynamic_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDynamicLinkClick();
            }
        });
        findViewById(R.id.btn_share_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteClick();
            }
        });

        handleDeepLink();

    }

    private void onDynamicLinkClick() {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(getPromotionDeepLink())
                .setDynamicLinkDomain("n3a95.app.goo.gl")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(getPackageName()).build())

                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder(getPackageName())
                                .setMinimumVersion(125)
                                .build())
                .setIosParameters(
                        new DynamicLink.IosParameters.Builder(getPackageName())
                                .setAppStoreId("123456789")
                                .setMinimumVersion("1.0.1")
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("orkut")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())
                .setItunesConnectAnalyticsParameters(
                        new DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                                .setProviderToken("123456")
                                .setCampaignToken("example-promo")
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("Example of a Dynamic Link")
                                .setDescription("This link works whether the app is installed or not!")
                                .build())

                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            try {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                sendIntent.setType("text/plain");
                                startActivity(Intent.createChooser(sendIntent, "Share"));
                            } catch (ActivityNotFoundException ignored) {
                            }
                        } else {
                            Log.w(TAG, task.toString());
                        }
                    }
                });
    }

    private void onInviteClick() {
        Intent intent = new AppInviteInvitation.IntentBuilder("Something")
                .setMessage("Message")
                .setDeepLink(getPromotionDeepLink())
                .setEmailHtmlContent("<a href='%%APPINVITE_LINK_PLACEHOLDER%%'>Buy ticket</a>")
                .setEmailSubject("Title")
                /*
                .setAdditionalReferralParameters()
                .setAndroidMinimumVersionCode()
                .setGoogleAnalyticsTrackingId()
                .setOtherPlatformsTargetApplication()
                */
                .build();
        startActivityForResult(intent, REQ_CODE_INVITE);
    }

    private void handleDeepLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        if (pendingDynamicLinkData == null) {
                            Log.d(TAG, "No have dynamic link");
                            return;
                        }
                        Uri deepLink = pendingDynamicLinkData.getLink();
                        Log.d(TAG, "deepLink: " + deepLink);

                        String segment = deepLink.getLastPathSegment();
                        switch (segment) {
                            case SEGMENT_PROMOTION:
                                String code = deepLink.getQueryParameter(KEY_CODE);
                                showPromotionDialog(code);
                                break;
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    private Uri getPromotionDeepLink() {
        // Generate promotion code
        String promotionCode = "DF3DY1";
        // https://ted.com/promotion?code=DF3DY1
        return Uri.parse("https://ted.com/" + SEGMENT_PROMOTION + "?" + KEY_CODE + "=" + promotionCode);
    }

    private void showPromotionDialog(String code) {
        new AlertDialog.Builder(this)
                .setMessage("Receive promotion code: " + code)
                .setPositiveButton("Confirm", null)
                .create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQ_CODE_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

}
