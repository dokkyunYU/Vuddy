package com.b305.vuddy.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.b305.vuddy.R

class ProfileFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        view.findViewById<ImageView>(R.id.iv_map).setOnClickListener {
            it.findNavController().navigate(R.id.action_profileFragment_to_mapFragment)
        }
        
        view.findViewById<ImageView>(R.id.iv_friend).setOnClickListener {
            it.findNavController().navigate(R.id.action_profileFragment_to_friendFragment)
        }
        
        view.findViewById<ImageView>(R.id.iv_message).setOnClickListener {
            it.findNavController().navigate(R.id.action_profileFragment_to_messageFragment)
        }
        
        view.findViewById<ImageView>(R.id.iv_write).setOnClickListener {
            val bottomSheetFragment = WriteFeedFragment()
            bottomSheetFragment.show(parentFragmentManager, "bottomSheetTag")
        }
        
        return view
    }
}