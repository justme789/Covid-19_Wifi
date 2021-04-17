package com.example.covidtracer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<String> bssid = new ArrayList<>();
    private TextView going;
    private TextView usage;
    private EditText post;
    private ProgressBar progressBar;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Realm.init(getContext());
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        post = view.findViewById(R.id.post);
        going = view.findViewById(R.id.goingTo);
        usage = view.findViewById(R.id.results);
        usage.setText("eg: If you're going to KCL, you should try typing in Strand though KCl could work it is recommended to check a street or a whole borough since your commute and the people you run into will be in that area");
        progressBar = view.findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        view.setOnClickListener(this);
        post.clearFocus();
        post.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    going.setAlpha(0.5f);
                    usage.setAlpha(0.5f);
                    ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) post.getLayoutParams();
                    params.height = (int) (post.getHeight() * 1.3);
                    params.width = (int) (post.getWidth() * 1.2);
                    post.setLayoutParams(params);
                } else {
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    going.setAlpha(1.0f);
                    usage.setAlpha(1.0f);
                    ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) post.getLayoutParams();
                    params.height = (int) (post.getHeight() / 1.3);
                    params.width = (int) (post.getWidth() / 1.2);
                    post.setLayoutParams(params);
                }
            }
        });
        post.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    usage.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    post.clearFocus();
                    Serach serach = new Serach();
                    serach.execute(post, view);
                }
                return false;
            }
        });
        return view;
    }

    public void onClick(View v) {
        post.clearFocus();
    }

    private class Serach extends AsyncTask<View, String, View> {
        String display;
        String info;
        int people;

        @Override
        protected View doInBackground(View... strings) {
            OkHttpClient client = new OkHttpClient();
            Request lonLatRequest = new Request.Builder()
                    .url("https://api.wigle.net/api/v2/network/geocode?addresscode=" + ((EditText) (strings[0])).getText().toString())
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization",
                            "Basic QUlEOTBjNDYzOTkxY2U1NmViNmNhNzg0MjcyNTlmN2I4ZjI6MDAyOWIyMmNlOTZiZGY0OThmODA1YjYzNTBlYzIzN2U=")
                    .get().build();
            JSONArray bound = new JSONArray();
            display = "";
            try (Response lonLatresponse = client.newCall(lonLatRequest).execute()) {
                String lonLatMainBod = lonLatresponse.body().string();
                JSONObject jsonObject = new JSONObject(lonLatMainBod);
                JSONArray lonLat = jsonObject.getJSONArray("results");
                for (int i = 0; i < lonLat.length(); i++) {
                    if (lonLat.getJSONObject(i) != null) {
                        bound = lonLat.getJSONObject(i).getJSONArray("boundingbox");
                        display = lonLat.getJSONObject(i).getString("display_name");
                        System.out.println(bound);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            wifiList(bound, null);
            return strings[1];
        }

        public String wifiList(JSONArray bound, String searchAfter) {
            OkHttpClient client = new OkHttpClient();
            HashMap<Integer, String> vals = new HashMap<>();
            DecimalFormat df = new DecimalFormat("#.###");

            double lonLow = 0;
            double lonHigh = 0;
            double latLow = 0;
            double latHigh = 0;
            try {
                lonLow = bound.getDouble(2);
                lonHigh = bound.getDouble(3);
                latLow = bound.getDouble(0);
                latHigh = bound.getDouble(1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url;
            if (searchAfter == null) {
                url = "https://api.wigle.net/api/v2/network/search?onlymine=false&latrange1=" + latLow + "&latrange2=" + latHigh + "&longrange1=" + lonLow + "&longrange2=" + lonHigh + "&freenet=false&paynet=false";
            } else {
                url = "https://api.wigle.net/api/v2/network/search?onlymine=false&latrange1=" + (latLow) + "&latrange2=" + (latHigh) + "&longrange1=" + (lonLow) + "&longrange2=" + lonHigh + "&freenet=false&paynet=false&searchAfter=" + searchAfter;
            }
            Request wifiRequest = new Request.Builder().url(
                    url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization",
                            "Basic QUlEOTBjNDYzOTkxY2U1NmViNmNhNzg0MjcyNTlmN2I4ZjI6MDAyOWIyMmNlOTZiZGY0OThmODA1YjYzNTBlYzIzN2U=")
                    .get().build();
            try (Response response = client.newCall(wifiRequest).execute()) {
                String mainBod = response.body().string();
                JSONObject jsonObject = new JSONObject(mainBod);
                int resultCount = (int) jsonObject.get("resultCount");
                JSONArray wifiList = jsonObject.getJSONArray("results");
                for (int i = 0; i < wifiList.length(); i++) {
                    if (wifiList.getJSONObject(i) != null) {
                        bssid.add(wifiList.getJSONObject(i).getString("netid"));
                    }
                }
                if (resultCount == 100) {
                    //wifiList(lat,lon,nextPage);
                }
                double latDiff = Math.abs(latLow - latHigh) * 110;
                double lonDiff = Math.abs(lonLow - lonHigh) * (110 * Math.cos(Math.toRadians(latHigh)));
                double density = 15;
                people = MainActivity.searchCrowd(bssid);
                if (people != 0) {
                    density = ((latDiff * lonDiff) * 1000) / people;
                }
                Intent pop = new Intent(getContext(), SearchPopUp.class);
                if (density <= 3) {
                    pop.putExtra("val", 2);
                    //startActivity(pop);
                    info = "This location is crowded to the brim, going there will guarantee that you're unable to maintain social distancing";
                } else if (density > 3 && density <= 13) {
                    pop.putExtra("val", 1);
                    //startActivity(pop);
                    info = "This location is a bit crowded and you will find yourself unable to maintain social distancing quite a lot";
                    //return "Maybe not\nThis location is a bit crowded and you will find yourself unable to maintain social distancing quite a lot";
                } else {
                    pop.putExtra("val", 0);
                    //startActivity(pop);
                    info = "This locations is safe and you should be able to maintain your distance. Please stay safe!";
                    //return "Definitely Not\n This location is crowded to the brim, going there will guarantee that you're unable to maintain social distancing";
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return "No Information is Available on that location. Please stay safe";
        }

        @Override
        protected void onPostExecute(View v) {
            super.onPostExecute(v);
            v.findViewById(R.id.results).setVisibility(View.VISIBLE);
            v.findViewById(R.id.progressBar2).setVisibility(View.GONE);
            SpannableString main = new SpannableString(display + "\n\n" + info + "\n" + "There are approximately " + people + " people here.");
            StyleSpan bold = new StyleSpan(Typeface.BOLD);
            usage.setMovementMethod(new ScrollingMovementMethod());
            main.setSpan(new AbsoluteSizeSpan(24, true), 0, display.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            main.setSpan(bold, 0, display.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            usage.setTextColor(Color.WHITE);
            usage.setText(main);
            usage.clearFocus();
        }

    }

}