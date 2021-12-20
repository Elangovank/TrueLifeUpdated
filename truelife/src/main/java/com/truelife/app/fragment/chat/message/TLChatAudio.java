package com.truelife.app.fragment.chat.message;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.truelife.R;
import com.truelife.base.TLFragmentManager;
import com.truelife.util.AppDialogs;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aravindh on 01-04-2017.
 * TLMenuList
 */

public class TLChatAudio extends DialogFragment {


    public static String TAG = TLChatAudio.class.getSimpleName();

    private FragmentActivity myContext;
    private TLFragmentManager myFragmentManager;
    private String AudioURL = "";
    private MediaPlayer myInComingAudioPlayer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the square_border for this fragment

        View aView = inflater.inflate(R.layout.audio_dialog, container, false);
        getBundle();
        initializeClassAndWidgets(aView);


        //  CheckClubList();
        setListeners();

        return aView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myInComingAudioPlayer.stop();


    }

    @Override
    public void onPause() {
        super.onPause();
        myInComingAudioPlayer.pause();
    }


    private void initializeClassAndWidgets(View aView) {

        try {
            myContext = getActivity();
            myFragmentManager = new TLFragmentManager(myContext);
            AppDialogs.INSTANCE.showProgressDialog(myContext);


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

            final ImageView myIncomingAudioPlayBtn = aView.findViewById(R.id.audio_play_img_btn);
            final ImageView myInComingAudioPauseBtn = aView.findViewById(R.id.audio_play_pause_img_btn);
            final SeekBar myIncomingPlayerSeekbar = aView.findViewById(R.id.seekBar);
            final TextView myAudioDurationTXT = aView.findViewById(R.id.audio_duration);
            final ImageButton myCloseBtn = aView.findViewById(R.id.close_button);

            myInComingAudioPlayer = new MediaPlayer();


            final Runnable UpdateSongTime = new Runnable() {
                public void run() {


                    InComingstartTime[0] = myInComingAudioPlayer.getCurrentPosition();
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
            String audioUrl = AudioURL;
            // Initialize a new media player instance
            myInComingAudioPlayer = new MediaPlayer();

            // Set the media player audio stream type
            myInComingAudioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //Try to play music/audio from url
            try {

                // Set the audio data source

                myInComingAudioPlayer.setDataSource(audioUrl);
                // Prepare the media player
                myInComingAudioPlayer.prepare();
                // Start playing audio from http url
                if (IncomeAudiopause[0] == true) {
                    AppDialogs.INSTANCE.hideProgressDialog();
                    myInComingAudioPlayer.seekTo((int) InComingstartTime[0]);
                    myInComingAudioPlayer.start();

                } else {
                    AppDialogs.INSTANCE.hideProgressDialog();
                    myInComingAudioPlayer.start();

                }


                InComingfinalTime[0] = myInComingAudioPlayer.getDuration();
                InComingstartTime[0] = myInComingAudioPlayer.getCurrentPosition();

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


            myInComingAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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
                    myInComingAudioPlayer.pause();
                    IncomeAudiopause[0] = true;
                    myInComingAudioPauseBtn.setVisibility(View.GONE);

                    myIncomingAudioPlayBtn.setVisibility(View.VISIBLE);
                    myIncomingAudioPlayBtn.setEnabled(true);
                }
            });


            myCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myInComingAudioPlayer.stop();
                    myFragmentManager.onBackPress();

                    /*if (mListener != null) {
                        mListener.returnData( );
                    }*/

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getBundle() {
        Bundle aBundle = getArguments();
        if (aBundle != null) {
            AudioURL = aBundle.getString("Chat_Audio_URL");
        }
    }


    private void setListeners() {


    }


    @Override
    public void onResume() {
        super.onResume();
    }


}
