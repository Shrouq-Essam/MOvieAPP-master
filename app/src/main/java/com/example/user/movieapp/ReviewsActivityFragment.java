package com.example.user.movieapp;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReviewsActivityFragment extends Fragment {

    private String LOG_TAG = ReviewsActivity.class.getSimpleName();
    private String text;

    public ReviewsActivityFragment() {
        text = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_reviews, container, false);
        if (getActivity().getIntent().hasExtra(Intent.EXTRA_TEXT))
            text = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);

        TextView textView = (TextView) rootView.findViewById(R.id.textView);
        textView.setText(text);

        return rootView;
    }



}
