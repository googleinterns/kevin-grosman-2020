/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.firstapp;

import android.app.Notification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

public class FirstFragment extends Fragment {

    TextView countTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View res = inflater.inflate(R.layout.fragment_first, container, false);
        countTextView = res.findViewById(R.id.textview_first);
        return res;
    }
    public void increment(View view) {
        Integer c = Integer.parseInt(countTextView.getText().toString());
        countTextView.setText((++c).toString());
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.random_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int c = Integer.parseInt(countTextView.getText().toString());
                NavDirections action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(c);
                NavHostFragment.findNavController(FirstFragment.this).navigate(action);
            }
        });
        view.findViewById(R.id.toast_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(getActivity(), R.string.toast_text, Toast.LENGTH_SHORT);
                myToast.show();
            }
        });

        view.findViewById(R.id.count_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment(view);
            }
        });

        view.findViewById(R.id.deceptive_random_parent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int c = Integer.parseInt(countTextView.getText().toString());
                NavDirections action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(c);
                NavHostFragment.findNavController(FirstFragment.this).navigate(action);
            }
        });
        view.findViewById(R.id.count_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment(view);
            }
        });
    }
}
