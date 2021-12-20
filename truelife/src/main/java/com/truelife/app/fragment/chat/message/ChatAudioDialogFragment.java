package com.truelife.app.fragment.chat.message;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.truelife.R;
import com.truelife.util.TLProgressDialog;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.widget.ListPopupWindow.WRAP_CONTENT;

/**
 * Created by kulanthaivel on 22-01-2019.
 */


@SuppressLint("ValidFragment")
public class ChatAudioDialogFragment extends DialogFragment {

    private final String LOG_TAG = ChatAudioDialogFragment.class.getSimpleName();
    FragmentActivity mContext;
    String AudioUR = "";
    ChatAudioDialogFragment myDialog;

    private AppCallback myAppCallback;
    TLProgressDialog myProgressDialog;

    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void returnData();
    }


    public ChatAudioDialogFragment(FragmentActivity activity, String aURL) {
        AudioUR = aURL;

    }


    // onCreate --> (onCreateDialog) --> onCreateView --> onActivityCreated
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");

        View promptsView = inflater.inflate(R.layout.audio_dialog, container, false);

        final double[] InComingstartTime = {0};
        double OutGoingstartTime = 0;
        final double[] InComingfinalTime = {0};
        double OutGoingfinalTime = 0;

        final int[] myInComingoneTimeOnly = {0};
        int myOutGoingoneTimeOnly = 0;


        final Handler myInComingAudioHandler = new Handler();
        Handler myOutGoingAudioHandler = new Handler();
        final boolean[] IncomeAudiopause = {false};


        // set prompts.xml to alertdialog builder


        final ImageView myIncomingAudioPlayBtn = promptsView.findViewById(R.id.audio_play_img_btn);
        final ImageView myInComingAudioPauseBtn = promptsView.findViewById(R.id.audio_play_pause_img_btn);
        final SeekBar myIncomingPlayerSeekbar = promptsView.findViewById(R.id.seekBar);
        final TextView myAudioDurationTXT = promptsView.findViewById(R.id.audio_duration);
        final ImageButton myCloseBtn = promptsView.findViewById(R.id.close_button);

        final MediaPlayer[] myInComingAudioPlayer = {new MediaPlayer()};


        getDialog().setCanceledOnTouchOutside(false);



        /*  // create alert dialog

         */


        if (getDialog().isShowing()) {
            myProgressDialog.hide();
        }


        final Runnable UpdateSongTime = new Runnable() {
            public  void run() {


                InComingstartTime[0] = myInComingAudioPlayer[0].getCurrentPosition();
                myAudioDurationTXT.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes((long) InComingstartTime[0]),
                        TimeUnit.MILLISECONDS.toSeconds((long) InComingstartTime[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) InComingstartTime[0])))
                );

                myIncomingPlayerSeekbar.setProgress((int) InComingstartTime[0]);
                myInComingAudioHandler.postDelayed(this, 100);


            }
        };

        myIncomingAudioPlayBtn.setEnabled(false);

        // The audio url to play
        // String audioUrl = "http://www.all-birds.com/Sound/western%20bluebird.wav";
        String audioUrl = AudioUR;
        // Initialize a new media player instance
        myInComingAudioPlayer[0] = new MediaPlayer();

        // Set the media player audio stream type
        myInComingAudioPlayer[0].setAudioStreamType(AudioManager.STREAM_MUSIC);
        //Try to play music/audio from url
        try {

            // Set the audio data source

            myInComingAudioPlayer[0].setDataSource(audioUrl);
            // Prepare the media player
            myInComingAudioPlayer[0].prepare();
            // Start playing audio from http url
            if (IncomeAudiopause[0] == true) {
                myInComingAudioPlayer[0].seekTo((int) InComingstartTime[0]);
                myInComingAudioPlayer[0].start();
            } else {
                myInComingAudioPlayer[0].start();

            }


            InComingfinalTime[0] = myInComingAudioPlayer[0].getDuration();
            InComingstartTime[0] = myInComingAudioPlayer[0].getCurrentPosition();

            if (myInComingoneTimeOnly[0] == 0) {
                myIncomingPlayerSeekbar.setMax((int) InComingfinalTime[0]);
                myInComingoneTimeOnly[0] = 1;
            }

            myInComingAudioPauseBtn.setVisibility(View.VISIBLE);
            myIncomingAudioPlayBtn.setVisibility(View.GONE);

            myIncomingPlayerSeekbar.setProgress((int) InComingstartTime[0]);
            myInComingAudioHandler.postDelayed(UpdateSongTime, 100);


            // Inform user for audio streaming

        } catch (IOException e) {
            // Catch the exception
            e.printStackTrace();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {

            e.printStackTrace();
        }


        myInComingAudioPlayer[0].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override

            public void onCompletion(MediaPlayer mediaPlayer) {


                IncomeAudiopause[0] = false;
                myIncomingAudioPlayBtn.setEnabled(true);
                myInComingAudioPauseBtn.setVisibility(View.GONE);

                myIncomingAudioPlayBtn.setVisibility(View.VISIBLE);
            }

        });

       /* myIncomingAudioPlayBtn.callOnClick();


        myIncomingAudioPlayBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {




            }
        });*/

        myInComingAudioPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myInComingAudioPlayer[0].pause();
                IncomeAudiopause[0] = true;
                myInComingAudioPauseBtn.setVisibility(View.GONE);

                myIncomingAudioPlayBtn.setVisibility(View.VISIBLE);
                myIncomingAudioPlayBtn.setEnabled(true);
            }
        });


        myCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myInComingAudioPlayer[0].stop();
                getDialog().dismiss();

                if (mListener != null) {
                    mListener.returnData();
                }

            }
        });


        /*    getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    myInComingAudioPlayer[0].stop();
                    if (mListener != null) {
                        mListener.returnData( );
                    }
                }
            });*/

      /*  getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    myInComingAudioPlayer[0].stop();
                }
            });*/


        return promptsView;
    }

    // If shown as dialog, set the width of the dialog window
    // onCreateView --> onActivityCreated -->  onViewStateRestored --> onStart --> onResume


    public interface AppCallback {
        void openFeeds();


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");

        if (getShowsDialog()) {


            // Set the width of the dialog to the width of the screen in portrait mode
            DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
            int dialogWidth = Math.min(metrics.widthPixels, metrics.heightPixels);
            Objects.requireNonNull(getDialog().getWindow()).setLayout(dialogWidth, WRAP_CONTENT);

        }
    }


    // If dialog is cancelled: onCancel --> onDismiss
    @Override
    public void onCancel(DialogInterface dialog) {
        Log.v(LOG_TAG, "onCancel");
    }

    // If dialog is cancelled: onCancel --> onDismiss
    // If dialog is dismissed: onDismiss
    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.v(LOG_TAG, "onDismiss");
    }
}