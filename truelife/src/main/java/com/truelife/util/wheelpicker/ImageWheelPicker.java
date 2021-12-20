package com.truelife.util.wheelpicker;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.truelife.app.model.Club;
import com.wheel.widget.adapter.PickerAdapter;
import com.wheel.widget.model.IPickerItemView;
import com.wheel.widget.model.PickerNode;
import com.wheel.widget.model.StringItemView;
import com.wheel.widget.picker.PicketOptions;
import com.wheel.widget.picker.WheelPicker;

import java.util.ArrayList;


/**
 * Created by kishore on 2019/9/20.
 */
public class ImageWheelPicker extends WheelPicker {

    private Context mContext;
    private ArrayList<Club.FriendList> mData = new ArrayList<>();
    ImageWheelPosition mCallBack;

    public ImageWheelPicker(Context context, ArrayList<Club.FriendList> list, ImageWheelPosition aCallback) {
        super(context, null, 0);
        mContext = context;
        this.mCallBack = aCallback;
        this.mData = list;
        init();
    }

    /*public ImageWheelPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageWheelPicker(Context context, AttributeSet attrs, int defStyleAttr) {

    }*/

    private void init() {

        final PickerNode<IPickerItemView> flags = getFlags();
        if (flags == null) {
            return;
        }

        PickerAdapter adapter = new PickerAdapter() {
            @Override
            public int numberOfComponentsInWheelPicker(WheelPicker wheelPicker) {
                return 1;
            }

            @Override
            public int numberOfRowsInComponent(int component) {

                return flags.getNextLevel().size();
            }

            @Override
            public View onCreateView(ViewGroup parent, int row, int component) {

                return flags.getNextLevel().get(row).getData().onCreateView(parent);
            }

            @Override
            public void onBindView(ViewGroup parent, View convertView, int row, int component) {
                flags.getNextLevel().get(row).getData().onBindView(parent, convertView, row);
            }
        };


        setOptions(new PicketOptions.Builder()
                .backgroundColor(Color.parseColor("#FFFFFF"))
                .dividerColor(Color.parseColor("#FFFFFF"))
                .cyclic(true)
                .linkage(false)
                .build());
        setAdapter(adapter);

        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker parentView, int[] position) {
                mCallBack.selectedPostition(position[0], Integer.parseInt(mData.get(position[0]).getId()));
            }
        });
    }


    private PickerNode<IPickerItemView> getFlags() {

        PickerNode<IPickerItemView> continents = new PickerNode<IPickerItemView>(new StringItemView("0"));
        PickerNode<IPickerItemView> cnode;
        ArrayList<PickerNode<IPickerItemView>> pnode = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            cnode = new PickerNode<>(new FlagItemView(mContext, mData.get(i)));
            pnode.add(cnode);
        }
        continents.setNextLevel(pnode);
        return continents;
    }

}

