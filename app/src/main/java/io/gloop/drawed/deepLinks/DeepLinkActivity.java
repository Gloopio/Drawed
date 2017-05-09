package io.gloop.drawed.deepLinks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Alex Untertrifaller on 09.05.17.
 */

//@DeepLink("drawed://gloop.io/deepLink/{id}")
public class DeepLinkActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Intent intent = getIntent();
//        if (intent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
//            Bundle parameters = intent.getExtras();
//            String idString = parameters.getString("id");
//
//            Toast.makeText(this, "Received id: " + idString, Toast.LENGTH_LONG).show();
//        }
//        Toast.makeText(this, "Error getting the id", Toast.LENGTH_LONG).show();

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            if (segments.size() > 1) {
                String parameter1 = segments.get(1);
                Toast.makeText(this, "Received id: " + parameter1, Toast.LENGTH_LONG).show();
            }
        }
        Toast.makeText(this, "Error getting the id", Toast.LENGTH_LONG).show();
    }

//    @DeepLink("drawed://gloop.io/methodDeepLink/{id}")
//    public static Intent intentForDeepLinkMethod(Context context, Bundle extras) {
//        return new Intent(context, DeepLinkActivity.class);
//    }
}
