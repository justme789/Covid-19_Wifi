package com.example.covidtracer;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    /**
     * This class handles the GUI for the home menu
     * It handles the animation and text that will be
     * displayed to the user from the information
     * provided from param1
     */
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView crowdedness;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        /**
         * setting up different gui components that will be used to create a
         * pulsating animation
         */
        View home = inflater.inflate(R.layout.fragment_home, container, false);
        crowdedness = home.findViewById(R.id.around);
        View green = home.findViewById(R.id.viewGreen);
        View yellow = home.findViewById(R.id.viewYellow);
        View red = home.findViewById(R.id.viewRed);
        /**
         * The crowdedness is determined based on information provided from
         * the NHS track and trace app
         */
        if (mParam1 != null) {
            home.findViewById(R.id.progressBar).setVisibility(View.GONE);
            if (Integer.parseInt(mParam1) > 2500) {
                crowdedness.setWidth(400);
                crowdedness.setText("This area is extremely crowded. It is highly recommended you leave!");
                animate(red);
            } else if (Integer.parseInt(mParam1) > 650 && Integer.parseInt(mParam1) < 2500) {
                crowdedness.setWidth(400);
                crowdedness.setText("This area is mildly crowded. It would be safer to leave");
                animate(yellow);
            } else {
                crowdedness.setWidth(400);
                crowdedness.setText("This area is not crowded. It is safe to be here");
                animate(green);
            }
        }

        return home;
    }

    private void animate(View view) {
        //Animating the circles
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.7f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(1500);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(-1);
        animator.start();
    }
}