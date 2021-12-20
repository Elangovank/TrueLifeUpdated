package com.truelife.util.wheelpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.truelife.R;
import com.truelife.app.model.Club;
import com.truelife.util.Utility;
import com.wheel.widget.model.BasePickerItemView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


class FlagItemView extends BasePickerItemView<String> {

    private Club.FriendList mData;
    private Context mContext;

    private int mDefaultColor = 0xFFAAAAAA;
    private int mSelectColor = 0xFF333333;

    public FlagItemView(Context aContext, Club.FriendList data) {
        super("");
        this.mContext = aContext;
        this.mData = data;
    }

    private Bitmap getImageFromAssetsFile(Context context, String fileName) {
        InputStream istr = null;
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            istr = am.open(fileName);

            // Calculate inSampleSize
            options.inSampleSize = 6;

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeStream(istr, null, options);
            istr.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.flag_item, null);

        TextView textView = root.findViewById(R.id.name);
        CircleImageView imageView = root.findViewById(R.id.img);

        textView.setText(mData.getFullname());

        if (mData.getProfile_image() != null && !mData.getProfile_image().isEmpty()) {
            Utility.INSTANCE.loadImage(mData.getProfile_image(), imageView, R.drawable.ic_place_club);
        } else
            Utility.INSTANCE.loadPlaceHolder(mContext, Objects.requireNonNull(mData.getGender()), imageView);

        int[] colors = new int[]{mSelectColor, mDefaultColor};
        int[][] states = {{android.R.attr.state_selected}, {}};
        textView.setTextColor(new ColorStateList(states, colors));

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.textView = textView;
        viewHolder.imageView = imageView;
        root.setTag(viewHolder);
        return root;
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindView(ViewGroup parent, View convertView, int position) {
        Object object = convertView.getTag();
    }

    private static class ViewHolder {
        TextView textView;
        CircleImageView imageView;
    }
}
