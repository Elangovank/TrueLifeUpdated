package com.truelife.app.activity.feedpost.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.youtube.player.internal.c
import com.squareup.picasso.Picasso
import com.truelife.ClickListener
import com.truelife.R
import com.truelife.app.model.ComentsListModel
import com.truelife.util.DateUtil
import org.apache.commons.lang3.StringEscapeUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class CommentListAdapter(
    private val mContext: Context,
    var mList: ComentsListModel, val listener: ClickListener
) : RecyclerView.Adapter<CommentListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.fragment_comment_list_adapter, parent, false)
        return ViewHolder(v)

    }

    fun update(aList: ComentsListModel) {
        this.mList = aList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.commentsList!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val mValue = mList.commentsList!![position]

        holder.user_name.text = mValue.userName
        val aTitle: String = StringEscapeUtils.unescapeJava(mValue.comment!!)
        Log.e("Content", aTitle)
        holder.comment.text = aTitle
        if (mValue.likeCount!!.toInt() > 1)
            holder.like_count.text = mValue.likeCount!! + " Likes"
        else if (mValue.likeCount!!.toInt() == 1)
            holder.like_count.text = mValue.likeCount!! + " Like"
        holder.time_stamp.text = DateUtil.getTimeAgo( SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(mValue.commentDate))


        if (mValue.isUserLike.equals("1"))
            holder.like_img.setImageResource(R.drawable.ic_like_red)
        else
            holder.like_img.setImageResource(R.drawable.ic_like_outline)

        if (!mValue.userImage.isNullOrEmpty() && !mValue.userImage.equals(""))
            Picasso.get().load(mValue.userImage).placeholder(R.drawable.ic_male)
                .error(R.drawable.ic_male).into(holder.user_image)
        if(( mList.commentsList!!.size.equals(0))||( mList.commentsList!!.size.equals(1)))
        {
            holder.commentView.visibility=View.GONE
        }
        else
        {
            holder.commentView.visibility=View.VISIBLE

        }
        holder.likeLay.setOnClickListener {
            listener.click(position)


            if (mValue.isUserLike.equals("0")) {
                holder.likeLay.animate()
                    .scaleY(1.3f)
                    .scaleX(1.3f)
                    .setDuration(200)
                    .setInterpolator(LinearInterpolator())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator?) {
                            holder.likeLay.scaleX = 1f
                            holder.likeLay.scaleY = 1f
                            holder.like_img.setImageResource(R.drawable.ic_like_red)
                        }
                    })
            } else
                holder.like_img.setImageResource(R.drawable.ic_like_outline)
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val commentView:View
        val user_image: ImageView
        val like_img: ImageView
        val user_name: TextView
        val comment: TextView
        val like_count: TextView
        val time_stamp: TextView
        val likeLay: LinearLayout


        init {
            user_image = itemView.findViewById(R.id.profile_image)
            like_img = itemView.findViewById(R.id.like_img)
            user_name = itemView.findViewById(R.id.user_name)
            comment = itemView.findViewById(R.id.user_comment)
            like_count = itemView.findViewById(R.id.like_count)
            time_stamp = itemView.findViewById(R.id.time_stamp)
            likeLay = itemView.findViewById(R.id.like_lay)
            commentView =itemView.findViewById(R.id.commentView)
        }

    }
}
