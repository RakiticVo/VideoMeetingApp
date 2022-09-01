package com.example.videomeetingapp.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.videomeetingapp.R
import com.example.videomeetingapp.databinding.ItemContainerUserBinding
import com.example.videomeetingapp.listeners.UsersListener
import com.example.videomeetingapp.models.User
import com.example.videomeetingapp.utilities.Constants
import java.util.*
import kotlin.collections.ArrayList

/* TODO: Create an UsersAdapter to Set Up a list of Users in RecyclerView*/
class UsersAdapter(
    private val context: Context,
    private val usersListener: UsersListener,
    private val users : ArrayList<User>,
    private val selectedUsers: ArrayList<User> = ArrayList(),
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    // TODO: Create Binding in ItemContainerUser Layout
    inner class UserViewHolder(val binding : ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : UserViewHolder {
        val binding = ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // TODO: Set up val
        val user = users[position]
        val random = Random()
        val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        val draw = GradientDrawable()
        val constants = Constants()
        draw.shape = GradientDrawable.OVAL
        draw.setColor(color)
        if (constants.isColorDark(color)){
            holder.binding.tvFirstChar.setTextColor(Color.WHITE)
        }else{
            holder.binding.tvFirstChar.setTextColor(Color.BLACK)
        }
        holder.binding.tvFirstChar.background = draw
        holder.binding.tvFirstChar.text = user.firstName!!.substring(0,1)
        holder.binding.tvUserName.text = String.format("%s %s", user.firstName, user.lastName)
        holder.binding.tvEmail.text = user.email
        // TODO: Click to call video with this User
        holder.binding.imgVideoMeeting.setOnClickListener {
            usersListener.initiateVideoMeeting(user)
        }
        // TODO: Click to call audio with this User
        holder.binding.imgAudioMeeting.setOnClickListener {
            usersListener.initiateAudioMeeting(user)
        }
        // TODO: Long click to choose this user for multiple calling
        holder.binding.userConstrainLayout.setOnLongClickListener {
            if (holder.binding.imgSelected.visibility != View.VISIBLE){
                selectedUsers.add(user)
                if (constants.isColorDark(color)){
                    holder.binding.imgSelected.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                }else{
                    holder.binding.imgSelected.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                }
                holder.binding.imgSelected.background = draw
                holder.binding.imgSelected.visibility = View.VISIBLE
                holder.binding.imgVideoMeeting.visibility = View.GONE
                holder.binding.imgAudioMeeting.visibility = View.GONE
                usersListener.onMultipleUsersAction(true)
            }
            true
        }
        // TODO: Click to choose this user for multiple calling when already have user
        holder.binding.userConstrainLayout.setOnClickListener {
            if (holder.binding.imgSelected.visibility == View.VISIBLE){
                selectedUsers.remove(user)
                if (constants.isColorDark(color)){
                    holder.binding.imgSelected.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                }else{
                    holder.binding.imgSelected.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                }
                holder.binding.imgSelected.background = draw
                holder.binding.imgSelected.visibility = View.GONE
                holder.binding.imgVideoMeeting.visibility = View.VISIBLE
                holder.binding.imgAudioMeeting.visibility = View.VISIBLE
                if (selectedUsers.size <= 0){
                    usersListener.onMultipleUsersAction(false)
                }
            }else{
                if (selectedUsers.size > 0){
                    selectedUsers.add(user)
                    if (constants.isColorDark(color)){
                        holder.binding.imgSelected.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                    }else{
                        holder.binding.imgSelected.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                    holder.binding.imgSelected.background = draw
                    holder.binding.imgSelected.visibility = View.VISIBLE
                    holder.binding.imgVideoMeeting.visibility = View.GONE
                    holder.binding.imgAudioMeeting.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return this.users.size
    }

    fun getSelectedUsers(): ArrayList<User>{
        return selectedUsers
    }
}