package com.truelife.util;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.truelife.R;
import com.truelife.base.TLFragmentManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

public class TLDownloadPdf {
    private FragmentActivity myContext;
    private boolean isOpenFile = false;
    //private String PATH = "/data/data/com.collabdocs/pdfdownload";
    private File myShowTimeDirectory;
    private String myTitleStr = "", myModuleTitleStr = "";
    private TLFragmentManager myFragmentManager;


    public TLDownloadPdf(FragmentActivity aActivity) {
        this.myContext = aActivity;
        // create a File object for the parent directory
        myShowTimeDirectory = myContext.getFilesDir();
        // Create Directory if not exists
        if (!myShowTimeDirectory.exists())
            myShowTimeDirectory.mkdirs();
        myFragmentManager = new TLFragmentManager(myContext);

    }


    /**
     * @param aFileUrlStr
     * @param aTitle
     * @param aModule
     */
    public void downloadAndOpenPdf(String aFileUrlStr, String aTitle, String aModule) {

        isOpenFile = true;
        myTitleStr = aTitle;

        myModuleTitleStr = aModule;
        downloadPdf(aFileUrlStr);
    }

    /**
     * @param aFileUrlStr
     */


    public void downloadAndSharePdf(String aFileUrlStr, String aTitle, String aModule) {

        isOpenFile = false;
        myTitleStr = aTitle;
        myModuleTitleStr = aModule;
        downloadPdf(aFileUrlStr);

    }


    private void downloadPdf(String aFileUrlStr) {

        if (!aFileUrlStr.equals("")) {

            String[] aUrlsplit = aFileUrlStr.split("/");
            String aFileName = aUrlsplit[aUrlsplit.length - 1];
            if (new File(myContext.getFilesDir(), aFileName).exists()) {

                // Open the file
                if (isOpenFile)
                    openFile(myShowTimeDirectory, aFileName);
                else
                    shareViaEmail(myShowTimeDirectory, aFileName);

            } else if (Utility.INSTANCE.isInternetAvailable(myContext))
                new DownloadFileFromURL(aFileName, aFileUrlStr).execute();
            else
                Toast.makeText(myContext, "Please check the internet connection", Toast.LENGTH_LONG).show();

        } else
            Toast.makeText(myContext, "No SD CARD Mounted", Toast.LENGTH_LONG).show();

    }

    /**
     * Check Whether Sd Present Or not
     */


    private boolean isSDPresent() {
        try {
            return android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
        } catch (Exception e) {

            e.printStackTrace();
            return false;


        }
    }

    /**
     * Download Async Task for files download
     */
    public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        private File outputFile;
        private ProgressDialog myProgressDialog;
        private String myFileNameStr = "", myFileUrlStr = "";

        public DownloadFileFromURL(String aFileName, String aFileUrlStr) {
            this.myFileNameStr = aFileName;
            this.myFileUrlStr = aFileUrlStr;
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("File", "Download Started");
            // setting progress dialog

            myProgressDialog = new ProgressDialog(myContext);
            // Set Image
            myProgressDialog.setIcon(R.drawable.area_icon);


            // Set progress dialog title
            myProgressDialog.setTitle("File Download");
            // set progress dialog message
            //  myProgressDialog.setMessage("Downloading " + myTitleStr + " - " + myModuleTitleStr + ".\nplease wait...");

            myProgressDialog.setMessage("File Downloading... ");

            myProgressDialog.setIndeterminate(false);

            myProgressDialog.setCanceledOnTouchOutside(false);

            // set progress dialog
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Show dialog
            myProgressDialog.show();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(myFileUrlStr);
                // Opening new URL connection
                URLConnection conection = url.openConnection();
                // Connecting to the connection
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // RTCHelper.deleteDir(myShowTimeDirectory);

                // create a File object for the output file
                outputFile = new File(myShowTimeDirectory, myFileNameStr);
                int i = 0;
                while (outputFile.exists()) {
                    i++;
                    myFileNameStr = "copy" + i + "of" + myFileNameStr;
                    outputFile = new File(myShowTimeDirectory, myFileNameStr);

                }

                // Output stream to write file
                OutputStream output = new FileOutputStream(outputFile);

                // Create new byte array data
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage

            myProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {


            try {
                // Dismiss Progress dialog
                if (myProgressDialog.isShowing())
                    myProgressDialog.dismiss();

                // Open the file
                if (isOpenFile)
                    openFile(myShowTimeDirectory, myFileNameStr);
                else

                    shareViaEmail(myShowTimeDirectory, myFileNameStr);

                Log.d("File", "Download complete");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Method to open Downloaded file
     *
     * @param aTieconDirectory
     * @param aFilename
     */
    private void openFile(File aTieconDirectory, String aFilename) {
        // set file path
        File file = new File(aTieconDirectory, aFilename);
        try {
            // Create URI path of given file
            Uri path = Uri.fromFile(file);

            String extension = android.webkit.MimeTypeMap
                    .getFileExtensionFromUrl(path.toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension);
            if (mimetype == null || mimetype == "")

                mimetype = "*/*";

            Bundle aBundle = new Bundle();
            aBundle.putString("PDF_FILE_PATH", file.getPath().toString().trim());

//            myFragmentManager.updateContent(new TLPDFViewer(), TLPDFViewer.TAG, aBundle);

        } catch (ActivityNotFoundException e) {
            try {

            } catch (Exception e1) {
                // RTCHelper.showAlert(myContext,
                // "No action was found to open the file", ALERT_FAILURE);
                e1.printStackTrace();
            }

        }

    }

    private void shareViaEmail(File aTieconDirectory, String aFilename) {

        try {
            // Opens email composer for sharing the Image
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emailIntent.setType("application/pdf");
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                    aFilename);
            // emailIntent.putExtra(Intent.EXTRA_TEXT, EMAIL_SHARE_MESSAGE);

            File file = new File(aTieconDirectory, aFilename);

            // Create URI path of given file
            Uri path = Uri.fromFile(file);
            emailIntent.putExtra(Intent.EXTRA_STREAM, path);

            final PackageManager pm = myContext.getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(
                    emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
            ResolveInfo best = null;
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.name.toLowerCase(Locale.ENGLISH)
                        .contains("mail"))
                    best = info;
            }

            if (best != null) {
                emailIntent.setClassName(best.activityInfo.packageName,
                        best.activityInfo.name);
                myContext.startActivityForResult(emailIntent, 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
