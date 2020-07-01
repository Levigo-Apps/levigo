//package com.levigo.levigoapp;
//
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.viewpager.widget.ViewPager;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.google.android.material.tabs.TabLayout;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//
//public class TabbedScanning extends AppCompatActivity {
//    private static final String TAG = TabbedScanning.class.getSimpleName();
//
////    private SectionsPagerAdapter mSectionsPagerAdapter;
//
//    /**
//     * The {@link ViewPager} that will host the section contents.
//     */
//    private ViewPager mViewPager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_tabbed_scanning);
//        setContentView(R.layout.activity_tabbed_scanning2);
//
//        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
//        viewPager.setAdapter(new ScanningFragmentPagerAdapter(getSupportFragmentManager(),
//                TabbedScanning.this));
//
//        // Give the TabLayout the ViewPager
//        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
//        tabLayout.setupWithViewPager(viewPager);
//
//    }
//
//    public void barcodeLookup(View view) {
////        String contents = result.getContents();
////        if(contents != null) {
////            Toast.makeText(this, "Success: Scanned " + result.getFormatName(), Toast.LENGTH_LONG).show();
////                mTextView.setText(contents);
//        String contents = "(01)00885672101114(17)180401(10)DP02149";
//        final TextView mTextView = findViewById(R.id.textView5);
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        String getInfo = null;
//        try {
//            getInfo = UDI.URL + URLEncoder.encode(contents, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        if (getInfo == null) return;
//
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, getInfo,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d(TAG, response);
//                        JSONObject json = null;
//                        try {
//                            json = new JSONObject(response);
//                            mTextView.setText(json.toString(2));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//            }
//        });
//
//        queue.add(stringRequest);
//    }
////        if(result.getBarcodeImagePath() != null) {
////            Log.d(TAG, "" + result.getBarcodeImagePath());
////            mImageView.setImageBitmap(BitmapFactory.decodeFile(result.getBarcodeImagePath()));
////        }
//}
