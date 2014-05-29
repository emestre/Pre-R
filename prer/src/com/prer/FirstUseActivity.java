package com.prer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.Window;

public class FirstUseActivity extends FragmentActivity {

	private PagerAdapter mPagerAdapter;
	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_first_use);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_use, menu);
		return true;
	}

	public void setCurrentItem(int item, boolean smoothScroll) {
		mPager.setCurrentItem(item, smoothScroll);
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment frag;
			switch (position) {
			case 0:
				frag = IntroFragment.create(R.drawable.prer_splash, R.string.welcome, position);
				break;
				
			case 1:
				frag = IntroFragment.create(R.drawable.search_btn_screenshot, R.string.search_tut, position);
				break;
				
			case 2:
				frag = IntroFragment.create(R.drawable.search_fields_screenshot, R.string.search_tut2, position);
				break;
				
			case 3:
				frag = IntroFragment.create(R.drawable.search_result_screenshot, R.string.search_tut3, position);
				break;
				
			case 4:
				frag = IntroFragment.create(0, R.string.start_searching, position);
				break;
				
			default:
				frag = IntroFragment.create(R.drawable.prer_splash, R.string.welcome, position);
				break;
			}
			
			return frag;
		}

		@Override
		public int getCount() {
			return 5;
		}
	}

}
