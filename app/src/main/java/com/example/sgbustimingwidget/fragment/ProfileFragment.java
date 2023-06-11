package com.example.sgbustimingwidget.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoAdapter;
import com.example.sgbustimingwidget.adapter.BusInfoItem;

import java.util.ArrayList;
import java.util.Map;

public class ProfileFragment extends Fragment {


    public ProfileFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button buttonSignIn = view.findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog profileDialog = (new AlertDialog.Builder(getActivity())).create();
                profileDialog.setMessage("Sike! This feature is not ready yet. If you really want it to be implemented please email me at victorpham1997@gmail.com.");
                profileDialog.show();
            }
        });

        return view;

    }
}