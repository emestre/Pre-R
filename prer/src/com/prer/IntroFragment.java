package com.prer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class IntroFragment extends Fragment {

	private TextView introCaption;
	private ImageView introImage;
	private Button nextBtn;

	private int imageId;
	private int captionId;
	private int pos;

	public static IntroFragment create(int imageId, int captionId, int position) {
		IntroFragment fragment = new IntroFragment(imageId, captionId, position);
		return fragment;
	}

	public IntroFragment(int imageId, int captionId, int pos) {
		this.imageId = imageId;
		this.captionId = captionId;
		this.pos = pos;
	}

	public IntroFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View view = inflater.inflate(R.layout.intro_fragment, container, false);
		introCaption = (TextView) view.findViewById(R.id.intro_caption);
		introImage = (ImageView) view.findViewById(R.id.intro_image);
		nextBtn = (Button) view.findViewById(R.id.intro_button);

		nextBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (pos == 4) {
					Intent intent = new Intent(getActivity(), HomeScreenActivity.class);
		    		startActivity(intent);
		    		getActivity().finish();
		    		
					// tutorial is finished, save boolean in shared preferences
		    		SharedPreferences firstRunPreference = 
		    				getActivity().getSharedPreferences(SplashScreenActivity.FIRST_RUN_PREF_NAME, 
		    				FragmentActivity.MODE_PRIVATE);
		    		Editor editor = firstRunPreference.edit();
		    		editor.putBoolean(SplashScreenActivity.IS_FIRST_RUN, false);
		    		editor.commit();
				}
				else
					((FirstUseActivity) getActivity()).setCurrentItem(pos+1, true);
			}
		});

		Resources res = getActivity().getResources();

		String pageText = res.getString(captionId);
		introCaption.setText(pageText);

		if (imageId != 0) {
			Drawable pageImage = res.getDrawable(imageId);
			introImage.setImageDrawable(pageImage);
		}
		
		if (pos == 4)
			nextBtn.setText(res.getString(R.string.intro_final_btn_text));

		return view;
	}

}
