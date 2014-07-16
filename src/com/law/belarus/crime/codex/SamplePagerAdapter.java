package com.law.belarus.crime.codex;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class SamplePagerAdapter extends PagerAdapter{
    
    public List<View> pages = null;
    
    public SamplePagerAdapter(List<View> pages){
        this.pages = pages;
    }
    
    @Override
    public Object instantiateItem(View collection, int position){
        View v = pages.get(position);
        
        TextView textView = (TextView) v.findViewWithTag(MainActivity.TEXT_ITEM_TAG);

		if (textView != null)
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.articleTextSize);
		
        
        ((ViewPager) collection).addView(v, 0);
        return v;
    }
    
    @Override
    public void destroyItem(View collection, int position, Object view){
        ((ViewPager) collection).removeView((View) view);
    }
    
    @Override
    public int getCount(){
        return pages.size();
    }
    
    @Override
    public boolean isViewFromObject(View view, Object object){
        return view.equals(object);
    }

    @Override
    public void finishUpdate(View arg0){
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1){
    }

    @Override
    public Parcelable saveState(){
        return null;
    }

    @Override
    public void startUpdate(View arg0){
    }

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
}
