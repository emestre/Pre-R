package com.prer.tests;

import com.prer.R;
import com.prer.tests.PrerViews.Clickable;

public class HomeScreenActivityModel {

    public static Clickable<SearchActivityModel> HomeSearchButton = new Clickable<SearchActivityModel>(SearchActivityModel.class, R.id.home_search_button);
    public static Clickable<HomeScreenActivityModel> CameraButton = new Clickable<HomeScreenActivityModel>(HomeScreenActivityModel.class, R.id.home_camera_button);

}

