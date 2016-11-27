package com.example.user.movieapp;


        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;

        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v7.app.AlertDialog;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.RatingBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.squareup.picasso.Picasso;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.ArrayList;
        import java.util.HashSet;
        import java.util.Set;

public class DetailsActivityFragment extends Fragment{

    private String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    private JSONObject jsonObject;
    private String movie_ID;
    private Button favourite;
    private Button review;
    private Button trailer;
    private TextView overView;
    private TextView dateView;
    private TextView titleView;
    private ImageView imageView;
    private RatingBar ratingBar;
        private String type_videos = "videos";
    private String type_reviews = "reviews";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        //get jsonObject from arguments
        try {
            jsonObject = new JSONObject(getArguments().getString(Intent.EXTRA_TEXT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        movie_ID = getID();

        //initialize all views
        favourite = (Button) rootView.findViewById(R.id.btn_fav);
        review = (Button) rootView.findViewById(R.id.btn_reviews);
        trailer = (Button) rootView.findViewById(R.id.btn_trailers);
        overView = (TextView) rootView.findViewById(R.id.tv_overview);
        dateView = (TextView) rootView.findViewById(R.id.tv_date);
        titleView = (TextView) rootView.findViewById(R.id.tv_title);
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        ratingBar = (RatingBar) rootView.findViewById(R.id.rating);

        //favourite button

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
             Picasso.with(getActivity()).load(getPoster()).into(imageView);

        titleView.setText(getTitle());


        overView.setText(getOverView());


        String date = getDate();
        dateView.setText(date.substring(0, date.indexOf('-')));

                ratingBar.setRating(Float.parseFloat(getVote())/2);

                trailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchTrailers();
            }
        });
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchReviews();
            }
        });
        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 {
                    addToFav();
                    Toast.makeText(getActivity(), "Added Successfully!", Toast.LENGTH_SHORT).show();

                }

                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm.findFragmentById(R.id.fragment) != null && getSortMethod() == getString(R.string.pref_sort_fav)) {
                    MainActivityFragment movieFragment = (MainActivityFragment) fm.findFragmentById(R.id.fragment);
                    movieFragment.onStart();
                }
            }
        });
    }

    private String getSortMethod(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortMethod = sharedPreferences.getString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_pop));
        return sortMethod;
    }



    private void addToFav(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        Set<String> hs = sharedPreferences.getStringSet(getString(R.string.pref_sort_fav),new HashSet<String>());
        hs.add(jsonObject.toString());
        edit.commit();
    }

    private void FetchReviews(){

        new AsyncReviewTrailer().execute(type_reviews);
    }

    private void LaunchWithReviews(JSONObject response) throws JSONException {
        String jsonResults = "results";
        String jsonAuthor = "author";
        String jsonContent = "content";
        String text = "";


        JSONArray arr = response.getJSONArray(jsonResults);
        for (int i = 0; i < arr.length(); i++) {
            text += "Author : " + arr.getJSONObject(i).getString(jsonAuthor) + ",\n\n";
            text += arr.getJSONObject(i).getString(jsonContent) + "\n";
            text += "----------------------\n\n";
        }
        if (text.equals(""))
            text = "There are no reviews for this film.";

        startActivity(new Intent(getActivity(), ReviewsActivity.class).putExtra(Intent.EXTRA_TEXT, text));
    }

    private void FetchTrailers(){
       new AsyncReviewTrailer().execute(type_videos);
    }

    private void showContextForTrailers(final ArrayList<Trailer> trailers){
        final String[] items = new String[trailers.size()];
        for (int i = 0; i < trailers.size(); i++) {
            items[i] = trailers.get(i).name;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick a trailer");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final Uri.Builder builder = new Uri.Builder()
                        .scheme("http")
                        .authority("youtube.com")
                        .appendPath("watch")
                        .appendQueryParameter("v", trailers.get(item).url);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(builder.toString())));
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private ArrayList<Trailer> getmeURLS(JSONObject root) throws JSONException {
        String jsonResults = "results";
        String jsonName = "name";
        String jsonURL = "key";
        ArrayList<Trailer> ans = new ArrayList<>();
        JSONArray arr = root.getJSONArray(jsonResults);
        for (int i = 0; i < arr.length(); i++) {
            String url,name;
            url = arr.getJSONObject(i).getString(jsonURL);
            name = arr.getJSONObject(i).getString(jsonName);
            ans.add(new Trailer(url,name));
        }
        return ans;
    }

    private String getID(){
        String jsonID = "id";
        try {
            return jsonObject.getString(jsonID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getTitle(){
        String jsonTitle = "title";
        try {
            return jsonObject.getString(jsonTitle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPoster(){
        String jsonPoster = "poster_path";
        String preUrl = "http://image.tmdb.org/t/p/w185/";
        try {
            return preUrl + jsonObject.getString(jsonPoster);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getOverView(){
        String jsonOverview = "overview";
        try {
            return jsonObject.getString(jsonOverview);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getVote(){
        String jsonVote = "vote_average";
        try {
            return jsonObject.getString(jsonVote);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDate(){
        String jsonDate = "release_date";
        try {
            return jsonObject.getString(jsonDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class Trailer{
        String url,name;
        Trailer(String url,String name){
            this.url = url;
            this.name = name;
        }
    }

    public class AsyncReviewTrailer extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = AsyncReviewTrailer.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            String response = null;

            try {
                final Uri.Builder builder = new Uri.Builder()
                        .scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movie_ID)
                        .appendPath(type)
                        .appendQueryParameter("api_key", "ab83c6762993bce15c2c1d3e05477c5c");
                URL url = new URL(builder.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line + "\n");

                if (buffer.length() == 0) {
                                      return null;
                }
                response = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return response + type;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.endsWith(type_videos)) {
                response = response.substring(0,response.length() - 6);
                try {
                    ArrayList<Trailer> urls = getmeURLS(new JSONObject(response));
                    showContextForTrailers(urls);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    response = response.substring(0, response.length() - 7);
                    LaunchWithReviews(new JSONObject(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}

