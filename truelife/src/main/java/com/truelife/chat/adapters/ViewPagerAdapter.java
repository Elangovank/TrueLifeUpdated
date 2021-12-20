package com.truelife.chat.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.truelife.chat.activities.main.calls.CallsFragment;
import com.truelife.chat.activities.main.chats.FragmentChats;
import com.truelife.chat.activities.main.contacts.ContactFragment;
import com.truelife.chat.activities.main.wimsg.WiMsgFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private int count = 3;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentChats();
           /* case 1:
                return new ContactFragment();*/
            case 1:
                return new CallsFragment();
            case 2:
                return new WiMsgFragment();
            default:
                throw new IllegalStateException("Not implemented Fragment exception");
        }

    }

    @Override
    public int getCount() {
        return count;

    }


}
