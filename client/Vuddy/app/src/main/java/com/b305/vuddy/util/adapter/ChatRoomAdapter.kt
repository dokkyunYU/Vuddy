package com.b305.vuddy.util.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.b305.vuddy.R
import com.b305.vuddy.view.fragment.ChatFragment
import com.b305.vuddy.model.Chat
import com.b305.vuddy.util.ChatSocket
import com.bumptech.glide.Glide

class ChatRoomAdapter(private val chatRoomList: ArrayList<Chat>) : RecyclerView.Adapter<ChatRoomAdapter.CustomViewHolder>() {

    private lateinit var chatSocket: ChatSocket
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_list, parent, false)
        return CustomViewHolder(view).apply {
            itemView.setOnClickListener {
                val curPos : Int = bindingAdapterPosition
                val chatRoom : Chat = chatRoomList[curPos]

                chatSocket.goChatting(chatRoom.nickname)

                val bundle = Bundle()
                bundle.putString("nickname", chatRoom.nickname)
                val chatFragment = ChatFragment()
                chatFragment.arguments = bundle
                Log.d("Chat", "CHATCHAT $bundle")

                Toast.makeText(parent.context, "${chatRoom.nickname} : ${chatRoom.message}", Toast.LENGTH_SHORT).show()
                it.findNavController().navigate(R.id.action_messageFragment_to_chatFragment, bundle)
            }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val currentItem = chatRoomList[position]
        val defaultProfile = R.drawable.bird
        Glide.with(holder.itemView)
            .load(currentItem.profileImage) // 불러올 이미지 url
            .placeholder(defaultProfile) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(defaultProfile) // 로딩 에러 발생 시 표시할 이미지
            .fallback(defaultProfile) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
            .circleCrop() // 동그랗게 자르기
            .into(holder.profileImage) // 이미지를 넣을 뷰
        holder.nickname.text = currentItem.nickname
        holder.lastChat.text = currentItem.message
        holder.lastTime.text = currentItem.time
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.friend_profile)
        val nickname: TextView = itemView.findViewById(R.id.friend_name)
        val lastChat: TextView = itemView.findViewById(R.id.last_chat)
        val lastTime: TextView = itemView.findViewById(R.id.last_time)
    }
}
