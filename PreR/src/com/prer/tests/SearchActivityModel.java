package com.prer.tests;

import com.prer.R;
import com.prer.tests.PrerViews.Clickable;
import com.prer.tests.PrerViews.TextField;

public class SearchActivityModel {

	public static Clickable<SearchActivityModel> SearchButton = new Clickable<SearchActivityModel>(SearchActivityModel.class, R.id.search_button);
    public static TextField<SearchActivityModel> ZipCodeEditText = new TextField<SearchActivityModel>(SearchActivityModel.class, R.id.zip_code_edittext);
    public static TextField<SearchActivityModel> ProcedureEditText = new TextField<SearchActivityModel>(SearchActivityModel.class, R.id.search_bar);
    public static Clickable<SearchActivityModel> ResultsList = new Clickable<SearchActivityModel>(SearchActivityModel.class, R.id.results_list);
}
