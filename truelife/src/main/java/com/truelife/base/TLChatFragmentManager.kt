package com.truelife.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.truelife.R

/**
 * Created by Elango on 29/01/19.
 */

class TLChatFragmentManager
/**
 * Constructor to Initiate fragment manager
 *
 * @param aContext FragmentActivity
 */
    (private val mContext: FragmentActivity) {

    val backstackCount: Int
        get() {
            val aFragmentManager = mContext.supportFragmentManager
            return aFragmentManager.backStackEntryCount
        }
    /**
     * Update the Current Fragment by passing the below parameters
     *
     * @param aFragment Fragment
     * @param tag       String
     * @param aBundle   Bundle
     */
    fun addContent(aFragment: Fragment, tag: String, aBundle: Bundle?) {
        try {

            Log.e("TAG Screen name", tag)

            // Initialise Fragment Manager
            val aFragmentManager = mContext.supportFragmentManager

            // Initialise Fragment Transaction
            val aTransaction = aFragmentManager.beginTransaction()

            if (aBundle != null) {
                aFragment.arguments = aBundle
            }

            // Add the selected fragment
            aTransaction.add(R.id.frame_container, aFragment, tag)

            // add the tag to the backstack
            aTransaction.addToBackStack(tag)


            //Repalce New Fragment with old
            //            aTransaction.replace(R.id.frame_container,aFragment,tag);
            //            aTransaction.addToBackStack(null);


            // Commit the Fragment transaction
            aTransaction.commit()

            // aTransaction.commitAllowingStateLoss();

            myLastTag = tag

            Log.i("LastTag", myLastTag)

        } catch (e: StackOverflowError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun replaceContent(aFragment: Fragment, tag: String, aBundle: Bundle?) {
        try {

            Log.e("TAG Screen name", tag)


            // Initialise Fragment Manager
            val aFragmentManager = mContext.supportFragmentManager

            // Initialise Fragment Transaction
            val aTransaction = aFragmentManager.beginTransaction()


            if (aBundle != null) {
                aFragment.arguments = aBundle
            }

            //Repalce New Fragment with old
            aTransaction.replace(R.id.frame_container, aFragment, tag)
            aTransaction.addToBackStack(tag)


            // Commit the Fragment transaction
            aTransaction.commit()

            // aTransaction.commitAllowingStateLoss();

            myLastTag = tag

            Log.i("LastTag", myLastTag)

        } catch (e: StackOverflowError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @SuppressLint("CommitTransaction")
    fun refreshContent(aFragment: Fragment) {
        try {
            // Initialise Fragment Manager
            val aFragmentManager = mContext.supportFragmentManager

            // Initialise Fragment Transaction
            val aTransaction = aFragmentManager.beginTransaction()

            aTransaction.detach(aFragment).attach(aFragment)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Clear All Fragments
     */
    fun clearAllFragments() {
        try {
            val aFragmentManager = mContext.supportFragmentManager
            for (i in 0 until aFragmentManager.backStackEntryCount) {
                aFragmentManager.popBackStack()
            }
        } catch (e: StackOverflowError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun oneStepBack() {
        val fts = mContext.supportFragmentManager.beginTransaction()
        val fragmentManager = mContext.supportFragmentManager
        if (fragmentManager.backStackEntryCount >= 2) {
            fragmentManager.popBackStackImmediate()
            fts.commit()
        }
    }


    fun removeFragment(aCount: Int) {
        val aFragmentManager = mContext.supportFragmentManager
        for (i in 0 until aCount) {
            aFragmentManager.popBackStack()
        }

    }

    fun onBackPress() {
        val count = mContext.supportFragmentManager.backStackEntryCount
        if (count == 1) {
//            mContext.onBackPressed()

            //additional code
//            (mContext as DOCDashboardActivity).openDrawer()
        } else {
            mContext.supportFragmentManager.popBackStack()
        }
    }

    companion object {

        /**
         * Last fragment tag
         */
        private var myLastTag = ""
    }
}
