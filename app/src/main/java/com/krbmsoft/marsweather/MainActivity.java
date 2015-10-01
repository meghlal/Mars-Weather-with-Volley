package com.krbmsoft.marsweather;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.krbmsoft.marsweather.app.MarsWeather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView mTxtDegrees, mTxtWeather, mTxtError;
    ImageView mImageView;

    MarsWeather helper = MarsWeather.getInstance();
    final static String RECENT_API_ENDPOINT = "http://marsweather.ingenology.com/v1/latest/";

    final static String
    FLICKR_API_KEY = "95121e1d84391ae26c837dbaa2d1e13f",
    IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&" +
                    "tags=cute,animal&tag_mode=all&api_key=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtDegrees = (TextView) findViewById(R.id.degrees);
        mTxtWeather = (TextView) findViewById(R.id.weather);
        mTxtError = (TextView) findViewById(R.id.error);

        mImageView = (ImageView) findViewById(R.id.main_bg);

        loadWeatherData();
        try {
            searchRandomImage();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void loadWeatherData() {
        RequestHelper request = new RequestHelper
                (Request.Method.GET, RECENT_API_ENDPOINT, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String minTemp, maxTemp, atmo;
                            int avgTemp;

                            response = response.getJSONObject("report");

                            minTemp = response.getString("min_temp"); minTemp = minTemp.substring(0, minTemp.indexOf("."));
                            maxTemp = response.getString("max_temp"); maxTemp = maxTemp.substring(0, maxTemp.indexOf("."));

                            avgTemp = (Integer.parseInt(minTemp)+Integer.parseInt(maxTemp))/2;

                            atmo = response.getString("atmo_opacity");


                            mTxtDegrees.setText(avgTemp+"°");
                            mTxtWeather.setText(atmo);

                        } catch (Exception e) {
                            txtError(e);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtError(error);
                    }
                });

        request.setPriority(Request.Priority.HIGH);
        helper.add(request);

    }
    private void txtError(Exception e) {
        mTxtError.setVisibility(View.VISIBLE);
        mTxtDegrees.setVisibility(View.INVISIBLE);
        mTxtWeather.setVisibility(View.INVISIBLE);
        e.printStackTrace();
    }

    private void searchRandomImage() throws Exception {
        if (FLICKR_API_KEY.equals(""))
            throw new Exception("You didn't provide a working Flickr API!");

        RequestHelper request = new RequestHelper
                (Request.Method.GET, IMAGES_API_ENDPOINT+ FLICKR_API_KEY, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray images = response.getJSONObject("photos").getJSONArray("photo");
                            int index = new Random().nextInt(images.length());

                            JSONObject imageItem = images.getJSONObject(index);

                            String imageUrl = "http://farm" + imageItem.getString("farm") +
                                    ".static.flickr.com/" + imageItem.getString("server") + "/" +
                                    imageItem.getString("id") + "_" + imageItem.getString("secret") + "_" + "c.jpg";

                            loadImg(imageUrl);

                        } catch (Exception e) { imageError(e); }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        imageError(error);
                    }
                });
        request.setPriority(Request.Priority.LOW);
        helper.add(request);
    }

    int mainColor = Color.parseColor("#FF5722");

    private void imageError(Exception e) {
        mImageView.setBackgroundColor(mainColor);
        e.printStackTrace();
    }
    private void loadImg(String imageUrl) {
        // Retrieves an image specified by the URL, and displays it in the UI


        ImageLoader imageLoader = MarsWeather.getInstance().getImageLoader();

        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                imageError(error);
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    mImageView.setImageBitmap(response.getBitmap());
                }
            }
        });



    }


}
