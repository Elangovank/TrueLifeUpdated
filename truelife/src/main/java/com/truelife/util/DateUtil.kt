package com.truelife.util

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import com.truelife.R
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Elango on 31/01/19.
 **/

object DateUtil {

    private val apiDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    fun currentDateAndTime(aDateFormat: String?): String? {
        try {
            val sdf: DateFormat = SimpleDateFormat(aDateFormat)
            return sdf.format(Date())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    val currentDate: String
        get() {
            val cal = Calendar.getInstance()
            return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
        }

    val currentDateAsTimeStamp: String
        get() {
            var tsLong = System.currentTimeMillis() / 1000
            return tsLong.toString()
        }

    val currentTime: String
        get() {
            val cal = Calendar.getInstance()
            cal.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
            return SimpleDateFormat("h:mm a", Locale.ENGLISH).format(cal.time)
        }

    val previous15DaysDate: String
        get() {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -15)
            return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
        }

    val after15DaysDate: String
        get() {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, +15)
            return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
        }

    val previousMonth: String
        get() {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1)
            return SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(cal.time)
        }

    /**
     * @param d Date Object
     * @return String in format dd-MM-yyyy
     */
    fun formatDisplayDate(d: Date): String? {
        var ds: String? = null
        try {
            ds = displayDateFormat.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ds
    }

    /**
     * @param ds Date String in format dd-MM-yyyy
     * @return instance of [Date]
     */
    fun parseDisplayDate(ds: String): Date? {
        var date: Date? = null
        try {
            date = displayDateFormat.parse(ds)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return date
    }

    /**
     * @param d Date Object
     * @return string in format yyyy-MM-dd
     */
    fun formatApiDate(d: Date): String? {
        var ds: String? = null
        try {
            ds = apiDateFormat.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ds
    }

    fun getApiDate(date: String): String {
        val d = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(date)// 06 Dec 2017
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time) // 2017-12-06
    }


    fun getDateNamefromDate(date: String): String {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date) // 2017-12-06
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time) // 06 Dec 2017
    }

    fun getDateNameFromDateTime(date: String): String {
        val d = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date) // 2017-12-06
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time) // 06 Dec 2017
    }

    fun getMonthformat(date: String): String {
        val d = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.ENGLISH
        ).parse(date) // 2017-12-06 10:30:00
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat(
            "E, MMM dd, h:mm a",
            Locale.ENGLISH
        ).format(cal.time) // Dec,06 10:30 AM
    }

    fun getupdateformat(date: String): String {
        val d = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.ENGLISH
        ).parse(date) // 2017-12-06 10:30:00
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat(
            "MMM dd, h:mm a",
            Locale.ENGLISH
        ).format(cal.time) // Dec,06 10:30 AM
    }

    @SuppressLint("SimpleDateFormat")
    fun getTLNotificationformat(date: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val aDate = sdf.parse(date)

//        Log.e("FFFF -->> ", date)
//        Log.e("FFFF -->> ", aDate.toString())
//        Log.e("FFFF -->> ", aDate.time.toString())
//        Log.e("FFFF H -->> ", DateUtils.getRelativeTimeSpanString(aDate!!.time, Date().time, DateUtils.HOUR_IN_MILLIS).toString())
//        Log.e("FFFF M -->> ", DateUtils.getRelativeTimeSpanString(aDate.time, Date().time, DateUtils.MINUTE_IN_MILLIS).toString())
//        Log.e("FFFF S -->> ", DateUtils.getRelativeTimeSpanString(aDate.time, Date().time, DateUtils.SECOND_IN_MILLIS).toString())
//        Log.e("FFFF 0 -->> ", DateUtils.getRelativeTimeSpanString(aDate.time, Date().time, 0).toString())

        return DateUtils.getRelativeTimeSpanString(aDate!!.time, Date().time, 0).toString()
    }

    fun getleaveformat(date: String): String {
        val d = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.ENGLISH
        ).parse(date) // 2017-12-06 10:30:00
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("MMM dd", Locale.ENGLISH).format(cal.time) // Dec,06 10:30 AM
    }

    fun getleaveformatwithYear(date: String): String {
        val d = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.ENGLISH
        ).parse(date) // 2017-12-06 10:30:00
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(cal.time) // Dec,06 10:30 AM
    }

    fun getNotificationformat(date: String): String {
        val d = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(date) // 2017-12-06 10:30
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("MMM dd", Locale.ENGLISH).format(cal.time) // Dec,06 10:30 AM
    }

    fun getNotificationformatwithYear(date: String): String {
        val d = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(date) // 2017-12-06 10:30
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(cal.time) // Dec,06 10:30 AM
    }

    fun getDashBoardformat(date: String): String {
        val d = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(date) // 2017-12-06 10:30
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("MMM dd", Locale.ENGLISH).format(cal.time) // Dec,06 10:30 AM
    }

    fun getDashBoardformatwithYear(date: String): String {
        val d = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(date) // 2017-12-06 10:30
        val cal = Calendar.getInstance()

        cal.time = d
        return SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(cal.time) // Dec,06 10:30 AM
    }

    fun getTime24to12(time: String): String? {
        try {
            val t = SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(time) //14:00
            return SimpleDateFormat("h:mm a", Locale.ENGLISH).format(t) //10:25 AM
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    fun getTime12to24(time: String): String? {
        try {
            val t = SimpleDateFormat("h:mm a", Locale.ENGLISH).parse(time)//10:25 AM
            return SimpleDateFormat("HH:mm", Locale.ENGLISH).format(t) //14:00
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    private val myFormatDate = SimpleDateFormat("dd MMM yyyy", Locale.US)
    fun findDifferenceofTwoDays(from_date: String, to_date: String): Long? {
        try {
            val fdo = myFormatDate.parse(from_date)
            val tdo = myFormatDate.parse(to_date)
            val d = tdo.time - fdo.time
            if (tdo.after(fdo) || (tdo == fdo)) {
                return (d / (24 * 60 * 60 * 1000)) + 1
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun findDifferenceofTwoHours(from_hour: String, to_hour: String): String? {
        try {
            val simpleDateFormat = SimpleDateFormat("HH:mm")
            val startDate = simpleDateFormat.parse(DateUtil.getTime12to24(from_hour))
            val endDate = simpleDateFormat.parse(DateUtil.getTime12to24(to_hour))

            var difference = endDate.time - startDate.time
            if (difference < 0) {
                val dateMax = simpleDateFormat.parse("24:00")
                val dateMin = simpleDateFormat.parse("00:00")
                difference = dateMax.time - startDate.time + (endDate.time - dateMin.time)
            }
            val days = (difference / (1000 * 60 * 60 * 24)).toInt()
            val hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
            val min =
                (difference - (1000 * 60 * 60 * 24 * days).toLong() - (1000 * 60 * 60 * hours).toLong()).toInt() / (1000 * 60)

            return String.format("%s:%s", hours, min)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * @param aSelectedDate     String
     * @param aCurrentFormat    String
     * @param aConversionFormat String
     * @return String
     */
    fun convertDateFormat(
        aSelectedDate: String,
        aCurrentFormat: String,
        aConversionFormat: String
    ): String {
        var aDate = ""
        try {
            if (aSelectedDate.isEmpty())
                return ""
            val d = SimpleDateFormat(aCurrentFormat, Locale.ENGLISH).parse(aSelectedDate)
            aDate = SimpleDateFormat(aConversionFormat, Locale.ENGLISH).format(d.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aDate
    }


    /**
     * @param context    Context
     * @param datePicker DatePickerDialog - DatePickerDialog.OnDateSetListener from_datePicker = new DatePickerDialog.OnDateSetListener()
     * @param hidePast   true - Hide Past Date, Show Future Date -- false - Show Past Date, Hide Future Date
     */

    fun showDateDialog(
        context: Context,
        datePicker: DatePickerDialog.OnDateSetListener,
        hidePast: Boolean
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog =
            DatePickerDialog(context, R.style.MyDatePicker, datePicker, year, month, dayOfMonth)
        dialog.setCancelable(false)

        if (hidePast)
            dialog.datePicker.minDate = Date().time
        else dialog.datePicker.maxDate = Date().time

        dialog.setTitle("")
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SELECT", dialog)
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "CANCEL", dialog)
        dialog.show()
    }

    /**
     * @param context         Context
     * @param SelectedDate    String (dd-MM-yyyy)
     * @param from_datePicker DatePickerDialog - DatePickerDialog.OnDateSetListener from_datePicker = new DatePickerDialog.OnDateSetListener()
     * @param hidePast   true - Hide Past Date, Show Future Date -- false - Show Past Date, Hide Future Date
     */

    fun showFromDateDialog(
        context: Context,
        SelectedDate: String,
        from_datePicker: DatePickerDialog.OnDateSetListener,
        hidePast: Boolean
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val date = SelectedDate.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val dialog = DatePickerDialog(
            context,
            R.style.MyDatePicker,
            from_datePicker,
            year,
            month,
            dayOfMonth
        )
        dialog.setCancelable(false)
        if (hidePast)
            dialog.datePicker.minDate = Date().time
        else dialog.datePicker.maxDate = Date().time

        // Selected Date
        dialog.datePicker.init(
            Integer.parseInt(date[2]),
            Integer.parseInt(date[1]) - 1,
            Integer.parseInt(date[0]),
            null
        )

        dialog.setTitle("")
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SELECT", dialog)
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "CANCEL", dialog)
        dialog.show()
    }

    /**
     * @param context       Context
     * @param format        SimpleDateFormat
     * @param fromDate      String (dd-MM-yyyy)
     * @param SelectedDate  String (dd-MM-yyyy)
     * @param to_datePicker DatePickerDialog - DatePickerDialog.OnDateSetListener from_datePicker = new DatePickerDialog.OnDateSetListener()
     */

    fun showToDateDialog(
        context: Context,
        format: String,
        fromDate: String,
        SelectedDate: String,
        to_datePicker: DatePickerDialog.OnDateSetListener
    ) {
        val sdf_date = SimpleDateFormat(format, Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val Selected =
            SelectedDate.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            val dialog = DatePickerDialog(
                context,
                R.style.MyDatePicker,
                to_datePicker,
                year,
                month,
                dayOfMonth
            )
            dialog.setCancelable(false)

            // Selected Date
            dialog.datePicker.init(
                Integer.parseInt(Selected[2]),
                Integer.parseInt(Selected[1]) - 1,
                Integer.parseInt(Selected[0]),
                null
            )

            dialog.datePicker.minDate = sdf_date.parse(fromDate).time
            dialog.datePicker.maxDate = Date().time

            dialog.setTitle("")
            dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SELECT", dialog)
            dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "CANCEL", dialog)
            dialog.show()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    /**
     * @param context  Context
     * @param listener TimePickerDialog.OnTimeSetListener
     */

    fun showTimeDialog(context: Context, listener: TimePickerDialog.OnTimeSetListener) {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog =
            TimePickerDialog(context, listener, hourOfDay, minute, false) // True - 24hours format
        dialog.setCancelable(false)
        dialog.setTitle("")
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SELECT", dialog)
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "CANCEL", dialog)
        dialog.show()
    }


    /**
     * @param context  Context
     * @param format 10:00 AM
     * @param interval 15,30, 45 ....
     */

    fun getTimeSlot(context: Context, format: String, interval: Int): ArrayList<String> {
        val slots = arrayListOf<String>()
        try {
            val df = SimpleDateFormat(format, Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startDate = cal.get(Calendar.DATE)
            while (cal.get(Calendar.DATE) == startDate) {
                slots.add(df.format(cal.time))
                cal.add(Calendar.MINUTE, interval)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return slots
    }

    /**
     * Get Age from DOB
     */

    fun getAge(year: Int, month: Int, day: Int): Int {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        dob.set(year, month, day)

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        Log.i("Age -->> ", age.toString())
        return age
    }


    fun getCurrentDTForamat(aDateFormat: String?): String? {
        try {
            val sdf: DateFormat = SimpleDateFormat(aDateFormat)
            return sdf.format(Date())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }


    private const val SECOND_MILLIS: Int = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    private fun currentDate(): Date {
        val calendar = Calendar.getInstance()
        return calendar.time
    }

    fun getTimeAgo(date: Date): String? {
        var time = date.time
        if (time < 1000000000000L) {
            time *= 1000
        }
        val now = currentDate().time
        if (time > now || time <= 0) {
            return "in the future"
        }
        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "Just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "A minute ago"
        } else if (diff < 60 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + " minutes ago"
        } else if (diff < 2 * HOUR_MILLIS) {
            "An hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + " hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "Yesterday"
        } else {
            (diff / DAY_MILLIS).toString() + " days ago"
        }
    }
}
