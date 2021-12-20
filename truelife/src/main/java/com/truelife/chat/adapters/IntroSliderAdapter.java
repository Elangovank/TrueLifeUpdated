package com.truelife.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import  com.truelife.R;
import com.truelife.chat.model.SliderModel;

import java.util.List;

public class IntroSliderAdapter extends PagerAdapter {

    private Context Mcontext;
    private List<SliderModel> theSlideItemsModelClassList;


    public IntroSliderAdapter(Context Mcontext, List<SliderModel> theSlideItemsModelClassList) {
        this.Mcontext = Mcontext;
        this.theSlideItemsModelClassList = theSlideItemsModelClassList;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) Mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View sliderLayout = inflater.inflate(R.layout.slider_item_layout,null);

        ImageView featured_image = sliderLayout.findViewById(R.id.my_featured_image);
        TextView caption_title = sliderLayout.findViewById(R.id.my_caption_title);
        TextView caption_desc = sliderLayout.findViewById(R.id.my_caption_description);

        featured_image.setImageResource(theSlideItemsModelClassList.get(position).getFeatured_image());
        caption_title.setText(theSlideItemsModelClassList.get(position).getThe_caption_Title());
        caption_desc.setText(theSlideItemsModelClassList.get(position).getThe_caption_Desc());
        container.addView(sliderLayout);
        return sliderLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return theSlideItemsModelClassList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }
}
