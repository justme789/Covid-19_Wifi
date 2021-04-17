package com.example.covidtracer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    /**
     * this class handles the GUI the user sees when they open
     * the profile menu of the app.
     * This class will provide information to ContactActivity and
     * PositiveActivity classes to display to the user.
     */

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Long mParam3;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @param param3 Parameter 3.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2, Long param3) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putLong(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getLong(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        //Components in Fragment
        TextView username = view.findViewById(R.id.usernameShow);
        Button contactNum = view.findViewById(R.id.contactNum);
        Button positiveNum = view.findViewById(R.id.positiveNum);
        Button logout = view.findViewById(R.id.logout);
        Intent contactActivity = new Intent(getContext(), ContactActivity.class);
        Intent positiveActivity = new Intent(getContext(), PositiveActivity.class);
        //mParam1: username from MainActivity
        username.setText(mParam1);
        //mParam2: number of contacts from MainActivity
        if (mParam2 == null) {
            contactActivity.putExtra("val", "0");
        } else {
            contactActivity.putExtra("val", mParam2);
        }
        //mParam3: number of positive contacts from MainActivity
        if (mParam3 == null) {
            positiveActivity.putExtra("val", "0");
        } else {
            positiveActivity.putExtra("val", mParam3);
        }
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Toast.makeText(view.getContext(), "bye", Toast.LENGTH_SHORT).show();
                                ((MainActivity) getActivity()).deleteUser();
                                onDestroy();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                Toast.makeText(view.getContext(), "stayyyy", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Are you sure you want to?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
        contactNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(contactActivity);
            }
        });
        positiveNum.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                startActivity(positiveActivity);
            }
        });
        return view;
    }
}