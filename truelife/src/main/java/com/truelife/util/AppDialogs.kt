package com.truelife.util

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.truelife.*
import com.truelife.api.AppServices
import com.truelife.app.ClubShareMultiSelectionAdapter
import com.truelife.app.activity.FeedShareActivity
import com.truelife.app.activity.feedpost.activity.PostFeedDirectActivity
import com.truelife.app.activity.feedpost.activity.TLScreenPostFeedActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.ClubListShareModel
import com.truelife.app.model.LikeList
import com.truelife.app.model.PublicFeedModel
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import kotlinx.android.synthetic.main.bottom_feed_menu_dialog.view.*
import kotlinx.android.synthetic.main.bottom_feed_menu_dialog.view.edit_post_LAY_view
import kotlinx.android.synthetic.main.bottom_user_more_dialog.view.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Elango on 31/01/19.
 **/


object AppDialogs {
    var mPlayer: SimpleExoPlayer? = null
    private var progressDialog: Dialog? = null
    private var dialog: Dialog? = null
    public var custom_dialog: Dialog? = null
    private var selection_dialog: Dialog? = null
    private var bottom_dialog: BottomSheetDialog? = null
    private var mProgressBar: ProgressBar? = null
    private var mCountTV: TextView? = null

    /**
     * Simple interface can be implemented for confirm an action via dialogs
     */
    interface ConfirmListener {
        fun yes()
    }

    interface OptionListener : ConfirmListener {
        fun no()
    }

    fun getPlayerInstance(): SimpleExoPlayer {

        return mPlayer!!
    }

    fun setVideoPlayer(con: Context) {
        if (mPlayer == null) {
            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
            val videoTrackSelectionFactory: TrackSelection.Factory =
                AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            mPlayer = ExoPlayerFactory.newSimpleInstance(con, trackSelector)
        }
    }

    /**
     * Confirm actions that are critical before proceeding
     *
     * @param c
     * @param text
     * @param l
     */
    @SuppressLint("InlinedApi")
    fun optionalAction(
        c: Context,
        text: String,
        l: OptionListener?,
        yes: String? = "YES",
        no: String? = "NO"
    ) {
        val builder = AlertDialog.Builder(c, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        builder.setTitle(c.resources.getString(R.string.app_name))
        builder.setMessage(text)

        builder.setPositiveButton(yes) { dialog, _ ->
            l?.yes()
            dialog.dismiss()
        }

        builder.setNegativeButton(no) { dialog, _ ->
            l?.no()
            dialog.dismiss()
        }

        builder.setOnCancelListener { dialogInterface -> dialogInterface.dismiss() }
        builder.create().show()
    }


    /**
     * Confirm actions that are critical before proceeding
     *
     * @param c
     * @param text
     * @param l
     */
    @SuppressLint("InlinedApi")
    fun confirmAction(c: Context, title: String, text: String, l: ConfirmListener?) {
        try {
            val alertDialog: AlertDialog
            val builder =
                AlertDialog.Builder(c, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
            builder.setTitle(title)
            builder.setMessage(text)

            builder.setPositiveButton(c.resources.getString(android.R.string.yes)) { dialog, which ->
                l?.yes()
                dialog.dismiss()
            }
            builder.setNegativeButton(c.resources.getString(android.R.string.no)) { dialog, which -> dialog.dismiss() }
            builder.setOnCancelListener { dialogInterface -> dialogInterface.dismiss() }
            alertDialog = builder.create()
            alertDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Confirm actions that are critical before proceeding
     *
     * @param c
     * @param text
     */
    @SuppressLint("InlinedApi")
    fun okAction(c: Context, text: String) {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(c, android.R.style.Theme_Material_Light_Dialog)
        builder.setTitle(c.resources.getString(R.string.app_name))
        builder.setMessage(text)

        builder.setPositiveButton(c.resources.getString(android.R.string.ok)) { dialog, which -> dialog.dismiss() }

        builder.setOnCancelListener { dialogInterface -> dialogInterface.dismiss() }
        alertDialog = builder.create()
        alertDialog.show()

    }


    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun showProgressDialog(context: Context) {
        hideProgressDialog()
        progressDialog = Dialog(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_progress_custom, null)

        val progressBar = view.findViewById<ProgressBar>(R.id.dialog_progress_bar)
        progressBar.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(context, R.color.app_blue_light), PorterDuff.Mode.SRC_IN
        )

        progressDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog!!.setContentView(view)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    fun hideProgressDialog() {

        try {
            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        } catch (e: Exception) {
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("InlinedApi")
    fun showcustomView(
        context: Context,
        layout: Int,
        isCancelable: Boolean,
        fullScreen: Boolean
    ): View? {
        try {
            hidecustomView()
            custom_dialog = if (fullScreen)
                Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
            else
                Dialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)

            custom_dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            custom_dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(layout, null)
            custom_dialog!!.setContentView(view)
            custom_dialog!!.setCancelable(isCancelable)
            custom_dialog!!.show()
            return view
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Bottom Dialog
     */
    fun showBottomView(context: Context, layout: Int, isCancelable: Boolean): View? {
        try {
            hidecustomView()
            bottom_dialog = BottomSheetDialog(context, R.style.BottomSheetDialog)
            val view = LayoutInflater.from(context).inflate(layout, null)
            bottom_dialog!!.setContentView(view)
            bottom_dialog!!.setCancelable(isCancelable)
            bottom_dialog!!.show()
            return view
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Show Ok Action Dialog
     * @param context Context
     * @param title Title
     * @param msg Message
     * @param action Positive Button Text
     * @param l ConfirmListener
     * @param isCancelable Close button view or hide
     */

    fun customOkAction(
        context: Context,
        title: String?,
        msg: String,
        action: String?,
        l: ConfirmListener?,
        isCancelable: Boolean
    ) {

        hidecustomView()

        val builder = AlertDialog.Builder(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_ok_action, null)

        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogMessage = view.findViewById(R.id.dialog_message) as TextView
        val dialogAction = view.findViewById(R.id.dialog_action_button) as TextView
        val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView

        builder.setCancelable(false)

        if (isCancelable)
            dialogClose.visibility = View.VISIBLE
        else dialogClose.visibility = View.GONE

        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title
        dialogMessage.text = msg

        dialogAction.text = if ((action == null)) context.getString(R.string.ok) else action
        dialogAction.setOnClickListener {
            l?.yes()
            custom_dialog!!.dismiss()
        }

        dialogClose.setOnClickListener {
            custom_dialog!!.dismiss()
        }

        builder.setView(view)
        custom_dialog = builder.create()
        custom_dialog!!.show()
    }

    fun customOkActionFullScreen(
        context: Context,
        title: String?,
        msg: String,
        action: String?,
        l: ConfirmListener?,
        isCancelable: Boolean
    ) {

        hidecustomView()

        val builder =
            AlertDialog.Builder(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_ok_action_full, null)

        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogMessage = view.findViewById(R.id.dialog_message) as TextView
        val dialogAction = view.findViewById(R.id.dialog_action_button) as TextView
        val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView

        builder.setCancelable(false)

        if (isCancelable)
            dialogClose.visibility = View.VISIBLE
        else dialogClose.visibility = View.GONE

        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title
        dialogMessage.text = msg

        dialogAction.text = if ((action == null)) context.getString(R.string.ok) else action
        dialogAction.setOnClickListener {
            l?.yes()
            custom_dialog!!.dismiss()
        }

        dialogClose.setOnClickListener {
            custom_dialog!!.dismiss()
        }

        builder.setView(view)
        custom_dialog = builder.create()
        custom_dialog!!.show()
    }

    /**
     * Show Double Action Dialog
     * @param context Context
     * @param title Title
     * @param msg Message
     * @param Positiveaction Positive Button Text
     * @param Negativeaction Negative Button Text
     * @param l OptionListener
     * @param isCancelable Close button view or hide
     * @param isOptionable Negative button background gray
     */

    fun customDoubleAction(
        context: Context,
        title: String?,
        msg: String,
        Positiveaction: String?,
        Negativeaction: String?,
        l: OptionListener?,
        isCancelable: Boolean,
        isOptionable: Boolean
    ) {

        try {
            hidecustomView()

            val builder = AlertDialog.Builder(context, R.style.BottomSheetDialog)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_double_action, null)

            val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
            val dialogMessage = view.findViewById(R.id.dialog_message) as TextView
            val dialogActionPositive = view.findViewById(R.id.dialog_action_positive) as TextView
            val dialogActionNegative = view.findViewById(R.id.dialog_action_negative) as TextView
            val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView
            custom_dialog
            builder.setCancelable(false)

            dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title
            dialogMessage.text = msg

            dialogActionPositive.text =
                if ((Positiveaction == null)) context.getString(R.string.ok) else Positiveaction
            dialogActionPositive.setOnClickListener {
                l?.yes()
                custom_dialog!!.dismiss()
            }

            if (isOptionable) {
                dialogActionNegative.setBackgroundResource(R.drawable.bg_circle_blue)
            }

            dialogActionNegative.text =
                if ((Negativeaction == null)) context.getString(R.string.cancel) else Negativeaction
            dialogActionNegative.setOnClickListener {
                l?.no()
                custom_dialog!!.dismiss()
            }

            if (isCancelable)
                dialogClose.visibility = View.VISIBLE
            else dialogClose.visibility = View.GONE

            dialogClose.setOnClickListener {
                custom_dialog!!.dismiss()
            }

            builder.setView(view)
            custom_dialog = builder.create()
            custom_dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hidecustomView() {
        if (custom_dialog != null && custom_dialog!!.isShowing) {
            custom_dialog!!.dismiss()
        }
    }

    /**
     * @param context
     */
    fun hidecustomProgressDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }

    /**
     * Hides the soft keyboard
     *
     * @param activity Activity
     */
    fun hideSoftKeyboard(activity: Activity, view: View) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Shows the soft keyboard
     *
     * @param a    Activity
     * @param view current EditText view
     */
    fun showSoftKeyboard(a: Activity, view: View) {
        val inputMethodManager =
            a.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        inputMethodManager.showSoftInput(view, 0)
    }

    fun showSnackbar(
        activity: Activity,
        view: View,
        msg: String,
        action: String? = activity.resources.getString(R.string.settings)
    ) {
        val snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction(action) {
            startActivityForResult(
                activity,
                Intent(android.provider.Settings.ACTION_SETTINGS),
                0,
                null
            )
        }
        snackbar.setActionTextColor(Utility.getColor(activity, R.color.app_green_light))
        snackbar.show()
    }

    fun showSnackbar(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * showCustomSnackbar
     */
    fun showCustomSnackbar(context: Context, view: View, msg: String) {
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
        val layout = snackbar.view as Snackbar.SnackbarLayout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val snackView = inflater.inflate(R.layout.inflate_custom_snackbar, null)

        val mLayout = snackView.findViewById<LinearLayout>(R.id.inflate_snackbar_layout)
        val mImage = snackView.findViewById<ImageView>(R.id.inflate_snackbar_image)
        val mText = snackView.findViewById<TextView>(R.id.inflate_snackbar_text)

        mText.text = msg

        layout.setPadding(0, 0, 0, 0)
        layout.addView(snackView, 0)
        snackbar.show()
    }

    /**
     *Short Toast
     */
    fun showToastshort(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     *Long Toast
     */
    fun showToastlong(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }


    fun showFromDatedialogwithToday(
        context: Context,
        datepickListner: DatePickerDialog.OnDateSetListener
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(context, datepickListner, year, month, dayOfMonth)
        dialog.datePicker.minDate = System.currentTimeMillis() - 60 * 60 * 1000
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SELECT", dialog)
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "", dialog)
        dialog.setTitle("")
        dialog.show()
    }

    @SuppressLint("InlinedApi")
    fun showTimedialog(context: Context, timepickListner: TimePickerDialog.OnTimeSetListener) {

        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(
            context,
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert,
            timepickListner,
            hourOfDay,
            minute,
            false
        )
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SELECT", dialog)
        dialog.setTitle("")
        dialog.show()
    }

    /**
     * Single Choice Selection
     */
    fun showSingleChoice(
        context: Context,
        title: String?,
        items: ArrayList<String>,
        callback: SingleChoiceAdapter.Callback,
        isCancelable: Boolean
    ) {

        val builder = AlertDialog.Builder(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_single_selection, null)

        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogRecyclerView = view.findViewById(R.id.dialog_recycler) as RecyclerView
        val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView

        dialogRecyclerView.layoutManager = LinearLayoutManager(context)
        dialogRecyclerView.adapter = SingleChoiceAdapter(context, items, callback)

        builder.setCancelable(false)

        if (isCancelable)
            dialogClose.visibility = View.VISIBLE
        else dialogClose.visibility = View.GONE

        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title

        dialogClose.setOnClickListener {
            selection_dialog!!.dismiss()
        }

        builder.setView(view)
        selection_dialog = builder.create()
        selection_dialog!!.show()
    }

    /**
     * Single Choice Selection
     */
    fun showSingleChoice(
        context: Context,
        title: String?,
        items: ArrayList<String>,
        callback: SingleChoiceAdapter.Callback,
        isCancelable: Boolean, isSearch: Boolean
    ) {

        val builder = AlertDialog.Builder(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_single_selection, null)

        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogRecyclerView = view.findViewById(R.id.dialog_recycler) as RecyclerView
        val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView
        val dialogSearchLay = view.findViewById(R.id.search) as LinearLayout
        val dialogSearch = view.findViewById(R.id.fragment_search) as EditText
        val dialogSearchCancel = view.findViewById(R.id.fragment_cancel) as TextView

        dialogRecyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = SingleChoiceAdapter(context, items, callback)
        dialogRecyclerView.adapter = adapter

        builder.setCancelable(false)

        if (isSearch) {
            dialogSearchCancel.setOnClickListener {
                hideSoftKeyboard(context as Activity, dialogClose)
                dialogSearch.text = null
                dialogSearchCancel.visibility = View.GONE
                dialogSearch.clearFocus()
            }
            dialogSearchLay.visibility = View.VISIBLE
            dialogSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        if (count > 0)
                            dialogSearchCancel.visibility = View.VISIBLE

                        adapter.filter.filter(s!!.toString()) {
                            showNoData(
                                view,
                                it == 0,
                                String.format("We couldn't find anything for\n'%s'", s)
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } else dialogSearchLay.visibility = View.GONE

        if (isCancelable)
            dialogClose.visibility = View.VISIBLE
        else dialogClose.visibility = View.GONE

        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title

        dialogClose.setOnClickListener {
            selection_dialog!!.dismiss()
        }

        builder.setView(view)
        selection_dialog = builder.create()
        selection_dialog!!.show()
    }

    fun hideSingleChoice() {
        if (selection_dialog != null && selection_dialog!!.isShowing)
            selection_dialog!!.dismiss()
    }

    fun hideBottomDialog() {
        if (bottom_dialog != null && bottom_dialog!!.isShowing)
            bottom_dialog!!.dismiss()
    }


    /**
     * Show Toast Message
     *
     * @param context Context
     * @param desc    String
     */
    fun showToastDialog(context: Context, desc: String) {
        Toast.makeText(context, desc, Toast.LENGTH_SHORT).show()
    }

    fun swipeRefColor(context: Context, swipe: SwipeRefreshLayout) {
        swipe.setColorSchemeColors(
            Utility.getColor(context, R.color.app_blue_dark),
            Utility.getColor(context, R.color.app_blue_light),
            Utility.getColor(context, R.color.app_gray_dark)
        )
    }

    /**
     * No Data Layout
     *
     * @param view View
     * @param show show/hide
     * @param icon Image
     * @param msg Message to show
     */

    fun showNoData(view: View, show: Boolean) {
        showNoData(view, show, null, null)
    }

    fun showNoData(view: View, show: Boolean, icon: Int?) {
        showNoData(view, show, icon, null)
    }

    fun showNoData(view: View, show: Boolean, msg: String?) {
        showNoData(view, show, null, msg)
    }

    fun showNoData(view: View, show: Boolean, icon: Int?, msg: String?) {
        try {
            val mNodataLayout = view.findViewById<LinearLayout>(R.id.layout_nodata_layout)
            val mNodataImage = view.findViewById<ImageView>(R.id.layout_nodata_image)
            val mNodataMessage = view.findViewById<TextView>(R.id.layout_nodata_message)

            if (show) {
                mNodataLayout.visibility = View.VISIBLE
                icon?.let { mNodataImage.setImageResource(it) }
                msg?.let { mNodataMessage.text = msg }
            } else mNodataLayout.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showBottomMenu(
        context: Context,
        array: Array<String>,
        callback: TLBottomOptionAdapter.Callback,
        text: String?, title: String?, info: String?
    ) {
        bottom_dialog = BottomSheetDialog(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_bottom_more_option, null)

        val mInfoView = view.findViewById<LinearLayout>(R.id.dialog_info)

        val titleView = view.findViewById<TextView>(R.id.dialog_title)
        titleView.text = if ((title == null)) context.getString(R.string.app_name) else title

        val infoView = view.findViewById<TextView>(R.id.dialog_message)
        if (info != null) {
            infoView.text = info
            infoView.visibility = View.VISIBLE
            mInfoView.visibility = View.VISIBLE
        } else {
            infoView.visibility = View.GONE
            mInfoView.visibility = View.GONE
        }

        val dialogRecyclerView = view.findViewById(R.id.dialog_bottom_recycler) as RecyclerView
        dialogRecyclerView.layoutManager = LinearLayoutManager(context)
        dialogRecyclerView.adapter = TLBottomOptionAdapter(context, array, callback)

        val dialogDismiss = view.findViewById(R.id.dialog_bottom_dismiss) as TextView
        if (text != null)
            dialogDismiss.text = text

        dialogDismiss.setOnClickListener {
            bottom_dialog!!.dismiss()
        }

        bottom_dialog!!.setContentView(view)
        bottom_dialog!!.show()
    }

    fun showBottomMenu(
        context: Context,
        array: Array<String>,
        callback: TLBottomOptionAdapter.Callback,
        text: String?
    ) {
        bottom_dialog = BottomSheetDialog(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_bottom_more_option, null)

        val dialogRecyclerView = view.findViewById(R.id.dialog_bottom_recycler) as RecyclerView

        dialogRecyclerView.layoutManager = LinearLayoutManager(context)
        dialogRecyclerView.adapter = TLBottomOptionAdapter(context, array, callback)

        val dialogDismiss = view.findViewById(R.id.dialog_bottom_dismiss) as TextView
        if (text != null)
            dialogDismiss.text = text

        dialogDismiss.setOnClickListener {
            bottom_dialog!!.dismiss()
        }

        bottom_dialog!!.setContentView(view)
        bottom_dialog!!.show()
    }

    fun alertDialogwebViw(myContext: Context, aURL: String) {
        try {
            hidecustomView()
            val builder = AlertDialog.Builder(myContext, R.style.BottomSheetDialog)
            val view =
                LayoutInflater.from(myContext).inflate(R.layout.terms_policy_webview, null)
            builder.setCancelable(false)
            val aWebView = view.findViewById<View>(
                R.id.webView
            ) as WebView
            val aCloseBTN = view.findViewById<View>(
                R.id.webview_close_button
            ) as AppCompatButton
            aWebView.loadUrl(aURL)
            aWebView.clearCache(true)
            aWebView.clearHistory()
            aWebView.settings.javaScriptEnabled = true
            aWebView.settings.javaScriptCanOpenWindowsAutomatically = true
            aWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }
            aCloseBTN.setOnClickListener {
                custom_dialog!!.dismiss()
            }
            builder.setView(view)
            custom_dialog = builder.create()
            custom_dialog!!.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun showFeedPostDialog(
        context: Context,
        mFragmentManager: TLFragmentManager?,
        mBottomNavigation: BottomNavigationView,
        callback: DialogInterface.OnCancelListener
    ) {
        custom_dialog = Dialog(context)
        custom_dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        custom_dialog!!.setContentView(R.layout.feed_dialog)
        custom_dialog!!.setOnCancelListener(callback)
        // set the background partial transparent
        custom_dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window: Window? = custom_dialog!!.window

        val params: WindowManager.LayoutParams = window!!.attributes
        params.gravity = Gravity.BOTTOM or Gravity.CENTER


        custom_dialog!!.setCanceledOnTouchOutside(true)

        // initialize the item of the dialog box, whose id is demo1
        val demodialog =
            custom_dialog!!.findViewById(R.id.cross) as View
        val post =
            custom_dialog!!.findViewById(R.id.post) as View
        val gallery =
            custom_dialog!!.findViewById(R.id.gallery) as View

        gallery.setOnClickListener {
            mBottomNavigation.menu.getItem(TLConstant.ADD).setIcon(R.drawable.post_unselect)
            custom_dialog!!.dismiss()

            val aIntent = Intent(context, PostFeedDirectActivity::class.java)
            context.startActivity(aIntent)
        }


        post.setOnClickListener {
            val aIntent = Intent(context, TLScreenPostFeedActivity::class.java)
            context.startActivity(aIntent)
            custom_dialog!!.dismiss()
            mBottomNavigation.menu.getItem(TLConstant.ADD).setIcon(R.drawable.post_unselect)
        }

        custom_dialog!!.show()
    }


    fun showProgressBarCounting(context: Context) {
        hideProgressBarCounting()
        progressDialog = Dialog(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_progress_custom_counting, null)

        val progressBar = view.findViewById<ProgressBar>(R.id.circularProgressbar)
        val progressTV = view.findViewById<TextView>(R.id.tv)
        mProgressBar = progressBar
        mCountTV = progressTV
        progressBar.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(context, R.color.app_blue_light), PorterDuff.Mode.SRC_IN
        )
        progressDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog!!.setContentView(view)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    fun hideProgressBarCounting() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    fun setProgressCount(count: Int) {
        mCountTV!!.text = count.toString() + "%"
        mProgressBar!!.progress = count
    }

    /**
     * Show Ok Action Dialog
     * @param context Context
     * @param title Title
     * @param msg Message
     * @param action Positive Button Text
     * @param l ConfirmListener
     * @param isCancelable Close button view or hide
     */

    fun customLogoutAction(
        context: Context,
        title: String?,
        msg: String,
        action: String?,
        l: ConfirmListener?,
        isCancelable: Boolean
    ) {
        bottom_dialog = BottomSheetDialog(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_logout_action, null)
        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogMessage = view.findViewById(R.id.dialog_message) as TextView
        val dialogAction = view.findViewById(R.id.dialog_ok) as TextView
        val dialogClose = view.findViewById(R.id.dialog_cancel) as TextView


        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title
        dialogMessage.text = msg

        dialogAction.setOnClickListener {
            l?.yes()
            bottom_dialog!!.dismiss()
        }

        dialogClose.setOnClickListener {
            bottom_dialog!!.dismiss()
        }

        bottom_dialog!!.setContentView(view)
        bottom_dialog!!.show()
    }

    fun customSuccessAction(
        context: Context,
        title: String?,
        msg: String,
        action: String?,
        l: ConfirmListener?,
        isCancelable: Boolean
    ) {

        hidecustomView()

        val builder = AlertDialog.Builder(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_success_action, null)

        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogMessage = view.findViewById(R.id.dialog_message) as TextView
        val dialogAction = view.findViewById(R.id.dialog_action_button) as TextView
        val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView

        builder.setCancelable(false)

        if (isCancelable)
            dialogClose.visibility = View.VISIBLE
        else dialogClose.visibility = View.GONE

        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title
        dialogMessage.text = msg

        dialogAction.text = if ((action == null)) context.getString(R.string.ok) else action
        dialogAction.setOnClickListener {
            l?.yes()
            custom_dialog!!.dismiss()
        }

        dialogClose.setOnClickListener {
            custom_dialog!!.dismiss()
        }

        builder.setView(view)
        custom_dialog = builder.create()
        custom_dialog!!.show()
    }


    fun show_feed_menu(
        callback: FeedMenuClickListener,
        feedList: PublicFeedModel.FeedList,
        mContext: Context
    ) {
        try {
            bottom_dialog = BottomSheetDialog(mContext, R.style.BottomSheetDialog)
            val view = LayoutInflater.from(mContext).inflate(R.layout.bottom_feed_menu_dialog, null)
            bottom_dialog!!.setContentView(view)
            bottom_dialog!!.setCancelable(true)
            bottom_dialog!!.setCanceledOnTouchOutside(true)

            val mUser = LocalStorageSP.getLoginUser(mContext)


            when (feedList.isFollow) {
                "0" -> {
                    view.unfollow_user_TET.setText("Follow this user")
                    view.unfollow_user_IMG.setImageResource(R.drawable.feed_follow_icon)
                }
                "1" -> {
                    view.unfollow_user_TET.setText("UnFollow this user")
                    view.unfollow_user_IMG.setImageResource(R.drawable.close_circle_white)
                }
            }


            if (feedList.userId.equals(mUser.mUserId)) {
                view.report_post_LAY.visibility = View.GONE
                view.report_post_LAY_view.visibility = View.GONE
                view.block_user_LAY.visibility = View.GONE
                view.block_user_LAY_view.visibility = View.GONE
                view.unfollow_user_LAY.visibility = View.GONE
                view.delete_post_LAY.visibility = View.VISIBLE
                view.delete_post_LAY_view.visibility = View.GONE
            } else {
                view.edit_post_LAY.visibility = View.GONE
                view.edit_post_LAY_view.visibility = View.GONE
                view.delete_post_LAY.visibility = View.GONE
                view.delete_post_LAY_view.visibility = View.GONE
                view.report_post_LAY.visibility = View.VISIBLE
                view.report_post_LAY_view.visibility = View.VISIBLE
                view.block_user_LAY.visibility = View.VISIBLE
                view.block_user_LAY_view.visibility = View.VISIBLE
                view.unfollow_user_LAY.visibility = View.VISIBLE
            }


            view.edit_post_LAY.setOnClickListener {
                callback.EditThisPost(feedList)
                bottom_dialog!!.dismiss()
            }
            view.hide_post_LAY.setOnClickListener {
                callback.HideThisPost(feedList)
                bottom_dialog!!.dismiss()
            }
            view.delete_post_LAY.setOnClickListener {
                callback.DeleteThisPost(feedList)
                bottom_dialog!!.dismiss()
            }
            view.report_post_LAY.setOnClickListener {
                callback.ReportThisPost(feedList)
                bottom_dialog!!.dismiss()
            }
            view.block_user_LAY.setOnClickListener {
                callback.BlockThisPost(feedList)
                bottom_dialog!!.dismiss()
            }
            view.unfollow_user_LAY.setOnClickListener {
                callback.FollowThisPost(feedList)
                bottom_dialog!!.dismiss()
            }


            bottom_dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun show_user_more(
        callback: UserMoreClickListener,
        id: String,
        mContext: Context
    ) {
        try {
            bottom_dialog = BottomSheetDialog(mContext, R.style.BottomSheetDialog)
            val view = LayoutInflater.from(mContext).inflate(R.layout.bottom_user_more_dialog, null)
            bottom_dialog!!.setContentView(view)
            bottom_dialog!!.setCancelable(true)
            bottom_dialog!!.setCanceledOnTouchOutside(true)

            val mUser = LocalStorageSP.getLoginUser(mContext)

            view.block_user.setOnClickListener {
                callback.BlockThisUser(id)
                bottom_dialog!!.dismiss()
            }
            view.report_user.setOnClickListener {
                callback.ReportThisUser(id)
                bottom_dialog!!.dismiss()
            }


            bottom_dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun ShowShareDialog(
        feedList: PublicFeedModel.FeedList,
        mContext: Context
    ) {
        try {
            bottom_dialog = BottomSheetDialog(mContext, R.style.BottomSheetDialog)
            val view = LayoutInflater.from(mContext).inflate(R.layout.bottom_share_layout, null)
            bottom_dialog!!.setContentView(view)
            bottom_dialog!!.setCancelable(false)
            bottom_dialog!!.setCanceledOnTouchOutside(true)


            val mFriendsPickerItems: ArrayList<String> = ArrayList()
            val mPublicPickerItems: ArrayList<String> = ArrayList()

            mPublicPickerItems.add("Area")
            mPublicPickerItems.add("City")
            mPublicPickerItems.add("State")
            mPublicPickerItems.add("Country")
            mPublicPickerItems.add("Internationally")

            var selectedValue = ""
            var clubsList: ClubListShareModel = ClubListShareModel()
            var adapter: ClubShareMultiSelectionAdapter = ClubShareMultiSelectionAdapter(clubsList)
            mFriendsPickerItems.add("Friends")
            mFriendsPickerItems.add("Friends of friends")

            val aWhereToPostFriends =
                view.findViewById<View>(R.id.numberFriendsPicker) as NumberPicker
            val aWhereToPostPublic =
                view.findViewById<View>(R.id.numberPublicPicker) as NumberPicker
            val header_title = view.findViewById<View>(R.id.header_title) as TextView
            val header_Ques = view.findViewById<View>(R.id.question) as TextView
            val header_image = view.findViewById<View>(R.id.header_image) as ImageView
            val header_share_back = view.findViewById<View>(R.id.back_share) as ImageView
            val public = view.findViewById<View>(R.id.share_public) as LinearLayout
            val friends = view.findViewById<View>(R.id.share_friends) as LinearLayout
            val club = view.findViewById<View>(R.id.share_club) as LinearLayout
            val otherApps = view.findViewById<View>(R.id.share_other_app) as LinearLayout
            val firstPage = view.findViewById<View>(R.id.first_page) as LinearLayout
            val clubRV = view.findViewById<View>(R.id.clubs_recycler) as RecyclerView
            val progress = view.findViewById<View>(R.id.progress_bar) as RelativeLayout
            val shareBtn = view.findViewById<View>(R.id.share_now_btn) as AppCompatButton
            val friends_public_lay =
                view.findViewById<View>(R.id.friends_public_lay) as LinearLayout
            val back = view.findViewById<View>(R.id.back_arrow) as ImageView
            val strarray = mFriendsPickerItems.toTypedArray()
            val strPubarray = mPublicPickerItems.toTypedArray()
            var source = ""

            friends.setOnClickListener {
                source = "2"
                header_Ques.text = mContext.getString(R.string.label_share_feed_public_friends)
                header_title.text = mContext.getString(R.string.label_share_friends)
                header_image.setImageResource(R.drawable.clubs_main_icon)
                firstPage.visibility = View.GONE
                clubRV.visibility = View.GONE
                friends_public_lay.visibility = View.VISIBLE
                aWhereToPostPublic.visibility = View.GONE
                aWhereToPostFriends.visibility = View.VISIBLE
                selectedValue = strarray[0]
                aWhereToPostFriends.minValue = 0 //from array first value
                aWhereToPostFriends.isMeasureWithLargestChildEnabled = true
                aWhereToPostFriends.wrapSelectorWheel = false
                aWhereToPostFriends.isVerticalFadingEdgeEnabled = true
                aWhereToPostFriends.isHorizontalFadingEdgeEnabled = true
                aWhereToPostFriends.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                aWhereToPostFriends.displayedValues = strarray
                aWhereToPostFriends.maxValue = strarray.size - 1 //to array last value
                aWhereToPostFriends.setOnValueChangedListener { picker, oldVal, newVal ->
                    selectedValue = strarray[newVal]
                }
            }

            public.setOnClickListener {
                source = "1"
                header_Ques.text = mContext.getString(R.string.label_share_feed_public_friends)
                header_title.text = mContext.getString(R.string.label_share_public)
                header_image.setImageResource(R.drawable.ic_public_new)
                firstPage.visibility = View.GONE
                clubRV.visibility = View.GONE
                friends_public_lay.visibility = View.VISIBLE
                aWhereToPostPublic.visibility = View.VISIBLE
                aWhereToPostFriends.visibility = View.GONE
                selectedValue = strPubarray[0]
                aWhereToPostPublic.minValue = 0 //from array first value
                aWhereToPostPublic.isMeasureWithLargestChildEnabled = true
                aWhereToPostPublic.wrapSelectorWheel = false
                aWhereToPostPublic.isVerticalFadingEdgeEnabled = true
                aWhereToPostPublic.isHorizontalFadingEdgeEnabled = true
                aWhereToPostPublic.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                aWhereToPostPublic.displayedValues = strPubarray
                aWhereToPostPublic.maxValue = strPubarray.size - 1 //to array last value
                aWhereToPostPublic.setOnValueChangedListener { picker, oldVal, newVal ->
                    selectedValue = strPubarray[newVal]
                }
            }

            otherApps.setOnClickListener {
                bottom_dialog!!.dismiss()

            }
            club.setOnClickListener {
                source = "3"
                header_Ques.text = mContext.getString(R.string.label_share_feed_club)
                header_title.text = mContext.getString(R.string.label_share_club)
                header_image.setImageResource(R.drawable.clubs_friend_icon)
                firstPage.visibility = View.GONE
                aWhereToPostFriends.visibility = View.GONE
                aWhereToPostPublic.visibility = View.GONE
                clubRV.visibility = View.VISIBLE
                friends_public_lay.visibility = View.VISIBLE
                val mUser = LocalStorageSP.getLoginUser(mContext)

                val mCase = GetUserClubDetailsCase(
                    mUser.mUserId!!,
                    ""
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                //progress.visibility = View.VISIBLE
                AppServices.execute(
                    mContext, object : ResponseListener {
                        override fun onResponse(r: Response?) {
                            if (r != null) {
                                if (r.response!!.isSuccess) {
                                    // progress.visibility = View.GONE
                                    val gridLayoutManager = LinearLayoutManager(mContext)
                                    clubRV.layoutManager = gridLayoutManager
                                    clubRV.isNestedScrollingEnabled = false
                                    clubsList = r as ClubListShareModel
                                    adapter = ClubShareMultiSelectionAdapter(clubsList)
                                    clubRV.adapter = adapter

                                } else
                                    progress.visibility = View.GONE
                            } else
                                progress.visibility = View.GONE
                        }
                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.ClubListShare,
                    ClubListShareModel::class.java
                )

                /*GetUserClubDetailsCase*/
            }

            back.setOnClickListener {
                firstPage.visibility = View.VISIBLE
                friends_public_lay.visibility = View.GONE
            }
            header_share_back.setOnClickListener {
                bottom_dialog!!.dismiss()
            }

            shareBtn.setOnClickListener {
                Log.e("clubs", adapter.getSelectedClubs().toString())
                val aList = adapter.getSelectedClubs()
                val club_ids: StringBuilder = StringBuilder()
                for (i in aList.indices)
                    club_ids.append(aList[i] + ",")


                val intent = Intent(mContext, FeedShareActivity::class.java)
                intent.putExtra("feed_id", feedList.id)
                intent.putExtra("level", selectedValue)
                intent.putExtra("clubid", club_ids.toString())
                intent.putExtra("source", source)
                intent.putExtra("feed", feedList)
                mContext.startActivity(intent)
                bottom_dialog?.dismiss()
            }




            bottom_dialog!!.show()
        } catch (e: Exception) {

            print(e.toString())
        }
    }

    fun ShowShareDialog(
        feedList: PublicFeedModel.FeedList,
        mContext: Context, imgView: ImageView
    ) {
        try {
            bottom_dialog = BottomSheetDialog(mContext, R.style.BottomSheetDialog)
            val view = LayoutInflater.from(mContext).inflate(R.layout.bottom_share_layout, null)
            bottom_dialog!!.setContentView(view)
            bottom_dialog!!.setCancelable(false)
            bottom_dialog!!.setCanceledOnTouchOutside(true)


            val mFriendsPickerItems: ArrayList<String> = ArrayList()
            val mPublicPickerItems: ArrayList<String> = ArrayList()

            mPublicPickerItems.add("Area")
            mPublicPickerItems.add("City")
            mPublicPickerItems.add("State")
            mPublicPickerItems.add("Country")
            mPublicPickerItems.add("Internationally")

            var selectedValue = ""
            var clubsList: ClubListShareModel = ClubListShareModel()
            var adapter: ClubShareMultiSelectionAdapter = ClubShareMultiSelectionAdapter(clubsList)
            mFriendsPickerItems.add("Friends")
            mFriendsPickerItems.add("Friends of friends")

            val aWhereToPostFriends =
                view.findViewById<View>(R.id.numberFriendsPicker) as NumberPicker
            val aWhereToPostPublic =
                view.findViewById<View>(R.id.numberPublicPicker) as NumberPicker
            val header_title = view.findViewById<View>(R.id.header_title) as TextView
            val header_Ques = view.findViewById<View>(R.id.question) as TextView
            val header_image = view.findViewById<View>(R.id.header_image) as ImageView
            val header_share_back = view.findViewById<View>(R.id.back_share) as ImageView
            val public = view.findViewById<View>(R.id.share_public) as LinearLayout
            val friends = view.findViewById<View>(R.id.share_friends) as LinearLayout
            val club = view.findViewById<View>(R.id.share_club) as LinearLayout
            val otherApps = view.findViewById<View>(R.id.share_other_app) as LinearLayout
            val firstPage = view.findViewById<View>(R.id.first_page) as LinearLayout
            val clubRV = view.findViewById<View>(R.id.clubs_recycler) as RecyclerView
            val progress = view.findViewById<View>(R.id.progress_bar) as RelativeLayout
            val shareBtn = view.findViewById<View>(R.id.share_now_btn) as AppCompatButton
            val friends_public_lay =
                view.findViewById<View>(R.id.friends_public_lay) as LinearLayout
            val back = view.findViewById<View>(R.id.back_arrow) as ImageView
            val strarray = mFriendsPickerItems.toTypedArray()
            val strPubarray = mPublicPickerItems.toTypedArray()
            var source = ""

            friends.setOnClickListener {
                source = "2"
                header_Ques.text = mContext.getString(R.string.label_share_feed_public_friends)
                header_title.text = mContext.getString(R.string.label_share_friends)
                header_image.setImageResource(R.drawable.clubs_main_icon)
                firstPage.visibility = View.GONE
                clubRV.visibility = View.GONE
                friends_public_lay.visibility = View.VISIBLE
                aWhereToPostPublic.visibility = View.GONE
                aWhereToPostFriends.visibility = View.VISIBLE
                selectedValue = strarray[0]
                aWhereToPostFriends.minValue = 0 //from array first value
                aWhereToPostFriends.isMeasureWithLargestChildEnabled = true
                aWhereToPostFriends.wrapSelectorWheel = false
                aWhereToPostFriends.isVerticalFadingEdgeEnabled = true
                aWhereToPostFriends.isHorizontalFadingEdgeEnabled = true
                aWhereToPostFriends.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                aWhereToPostFriends.displayedValues = strarray
                aWhereToPostFriends.maxValue = strarray.size - 1 //to array last value
                aWhereToPostFriends.setOnValueChangedListener { picker, oldVal, newVal ->
                    selectedValue = strarray[newVal]
                }
            }

            public.setOnClickListener {
                source = "1"
                header_Ques.text = mContext.getString(R.string.label_share_feed_public_friends)
                header_title.text = mContext.getString(R.string.label_share_public)
                header_image.setImageResource(R.drawable.ic_public_new)
                firstPage.visibility = View.GONE
                clubRV.visibility = View.GONE
                friends_public_lay.visibility = View.VISIBLE
                aWhereToPostPublic.visibility = View.VISIBLE
                aWhereToPostFriends.visibility = View.GONE
                selectedValue = strPubarray[0]
                aWhereToPostPublic.minValue = 0 //from array first value
                aWhereToPostPublic.isMeasureWithLargestChildEnabled = true
                aWhereToPostPublic.wrapSelectorWheel = false
                aWhereToPostPublic.isVerticalFadingEdgeEnabled = true
                aWhereToPostPublic.isHorizontalFadingEdgeEnabled = true
                aWhereToPostPublic.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                aWhereToPostPublic.displayedValues = strPubarray
                aWhereToPostPublic.maxValue = strPubarray.size - 1 //to array last value
                aWhereToPostPublic.setOnValueChangedListener { picker, oldVal, newVal ->
                    selectedValue = strPubarray[newVal]
                }
            }

            otherApps.setOnClickListener {
                bottom_dialog!!.dismiss()
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"

                if (feedList.mediaType.equals("video")) {
                    i.putExtra(
                        Intent.EXTRA_TEXT,
                        feedList.content + " " + feedList.media!![0].original
                    )
                    mContext.startActivity(Intent.createChooser(i, "Share via"))
                } else if (feedList.mediaType.equals("image")) {
                    imgView.invalidate()
                    var bitmap: Uri? = Uri.parse(feedList.media!![0].original)
                    var bitmap1: Uri? = Uri.parse(feedList.media!![0].original)
                    if (imgView.drawable != null) {
                        bitmap = FileProvider.getUriForFile(
                            mContext,
                            "com.truelife.provider",
                            Utility.getLocalBitmapUri(imgView.drawable.toBitmap(), mContext)
                        )
                    }
                    i.putExtra(
                        Intent.EXTRA_TEXT,
                        feedList.content + " " + feedList.media!![0].original
                    )

                    if (bitmap != null) {
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "image/*"
                        share.putExtra(Intent.EXTRA_STREAM, bitmap)
                        mContext.startActivity(Intent.createChooser(share, "Share Image"))
                    }
                } else {
                    i.putExtra(Intent.EXTRA_TEXT, feedList.content)
                    mContext.startActivity(Intent.createChooser(i, "Share via"))
                }


                //i.putExtra(Intent.EXTRA_TEXT, "U+1F601")
                //i.putExtra(Intent.EXTRA_TEXT,"http://res.cloudinary.com/drrs4nuuj/video/upload/v1603390080/zuqvtufzg8kejgxshohi.mp4")


            }
            club.setOnClickListener {
                source = "3"
                header_Ques.text = mContext.getString(R.string.label_share_feed_club)
                header_title.text = mContext.getString(R.string.label_share_club)
                header_image.setImageResource(R.drawable.clubs_friend_icon)
                firstPage.visibility = View.GONE
                aWhereToPostFriends.visibility = View.GONE
                aWhereToPostPublic.visibility = View.GONE
                clubRV.visibility = View.VISIBLE
                friends_public_lay.visibility = View.VISIBLE
                val mUser = LocalStorageSP.getLoginUser(mContext)

                val mCase = GetUserClubDetailsCase(
                    mUser.mUserId!!,
                    ""
                )
                val result =
                    Helper.GenerateEncrptedUrl(
                        BuildConfig.API_URL,
                        mCase!!
                    )
                Log.e("URL", result)
                //progress.visibility = View.VISIBLE
                AppServices.execute(
                    mContext, object : ResponseListener {
                        override fun onResponse(r: Response?) {
                            if (r != null) {
                                if (r.response!!.isSuccess) {
                                    // progress.visibility = View.GONE
                                    val gridLayoutManager = LinearLayoutManager(mContext)
                                    clubRV.layoutManager = gridLayoutManager
                                    clubRV.isNestedScrollingEnabled = false
                                    clubsList = r as ClubListShareModel
                                    adapter = ClubShareMultiSelectionAdapter(clubsList)
                                    clubRV.adapter = adapter

                                } else
                                    progress.visibility = View.GONE
                            } else
                                progress.visibility = View.GONE
                        }
                    },
                    result,
                    Request.Method.POST,
                    AppServices.API.ClubListShare,
                    ClubListShareModel::class.java
                )

                /*GetUserClubDetailsCase*/
            }

            back.setOnClickListener {
                firstPage.visibility = View.VISIBLE
                friends_public_lay.visibility = View.GONE
            }
            header_share_back.setOnClickListener {
                bottom_dialog!!.dismiss()
            }

            shareBtn.setOnClickListener {
                Log.e("clubs", adapter.getSelectedClubs().toString())
                val aList = adapter.getSelectedClubs()
                val club_ids: StringBuilder = StringBuilder()
                for (i in aList.indices)
                    club_ids.append(aList[i] + ",")


                val intent = Intent(mContext, FeedShareActivity::class.java)
                intent.putExtra("feed_id", feedList.id)
                intent.putExtra("level", selectedValue)
                intent.putExtra("clubid", club_ids.toString())
                intent.putExtra("source", source)
                intent.putExtra("feed", feedList)
                mContext.startActivity(intent)
                bottom_dialog?.dismiss()
            }




            bottom_dialog!!.show()
        } catch (e: Exception) {

            print(e.toString())
        }
    }

    fun ShowShareDialogDirect(
        mContext: Context, clickListener: ClickListener
    ) {
        try {
            bottom_dialog = BottomSheetDialog(mContext, R.style.BottomSheetDialog)
            val view = LayoutInflater.from(mContext).inflate(R.layout.bottom_share_layout, null)
            bottom_dialog!!.setContentView(view)
            bottom_dialog!!.setCancelable(false)
            bottom_dialog!!.setCanceledOnTouchOutside(false)

            val header_share_back = view.findViewById<View>(R.id.back_share) as ImageView
            val public = view.findViewById<View>(R.id.share_public) as LinearLayout
            val friends = view.findViewById<View>(R.id.share_friends) as LinearLayout
            val club = view.findViewById<View>(R.id.share_club) as LinearLayout
            val other = view.findViewById<View>(R.id.share_other_app) as LinearLayout
            val back = view.findViewById<View>(R.id.back_arrow) as ImageView
            header_share_back.visibility = View.GONE
            back.visibility = View.GONE
            other.visibility = View.GONE
            public.setOnClickListener {
                clickListener.click(1)
                bottom_dialog!!.dismiss()
            }
            friends.setOnClickListener {
                clickListener.click(2)
                bottom_dialog!!.dismiss()
            }

            club.setOnClickListener {
                clickListener.click(3)
                bottom_dialog!!.dismiss()
            }

            back.setOnClickListener {
            }
            header_share_back.setOnClickListener {
                clickListener.click(4)
                bottom_dialog!!.dismiss()
            }

            bottom_dialog!!.show()
        } catch (e: Exception) {

            print(e.toString())
        }
    }


    fun GetUserClubDetailsCase(
        aLoginId: String,
        aLastUpdateTime: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aLoginId)
            jsonParam1.put("last_updated_time", aLastUpdateTime)
            val jsonParam = JSONObject()
            jsonParam.put("UserClubsList", jsonParam1)
            Log.e("UserClubsList", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    fun showLikeList(
        context: Context,
        title: String?,
        items: ArrayList<LikeList>,
        callback: LikeListAdapter.Callback,
        isCancelable: Boolean, isSearch: Boolean
    ) {

        val builder = AlertDialog.Builder(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_like_list, null)

        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogRecyclerView = view.findViewById(R.id.dialog_recycler) as RecyclerView
        val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView
        val dialogSearchLay = view.findViewById(R.id.search) as LinearLayout
        val dialogSearch = view.findViewById(R.id.fragment_search) as EditText
        val dialogSearchCancel = view.findViewById(R.id.fragment_cancel) as TextView

        dialogRecyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = LikeListAdapter(context, items, callback)
        dialogRecyclerView.adapter = adapter

        builder.setCancelable(true)

        if (isSearch) {
            dialogSearchCancel.setOnClickListener {
                hideSoftKeyboard(context as Activity, dialogClose)
                dialogSearch.text = null
                dialogSearchCancel.visibility = View.GONE
                dialogSearch.clearFocus()
            }
            dialogSearchLay.visibility = View.VISIBLE
            dialogSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        if (count > 0)
                            dialogSearchCancel.visibility = View.VISIBLE

                        adapter.filter.filter(s!!.toString()) {
                            showNoData(
                                view,
                                it == 0,
                                String.format("We couldn't find anything for\n'%s'", s)
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } else dialogSearchLay.visibility = View.GONE

      /*  if (isCancelable)
            dialogClose.visibility = View.VISIBLE
        else dialogClose.visibility = View.GONE*/

        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title


        dialogTitle.text = if (items.size > 1) items.size.toString() + "  Like(s)" else items.size.toString() + "  Like"

        dialogClose.setOnClickListener {
            selection_dialog!!.dismiss()
        }

        builder.setView(view)
        selection_dialog = builder.create()
        selection_dialog!!.show()
    }

}