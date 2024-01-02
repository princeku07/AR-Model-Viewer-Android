package com.xperiencelabs.modelviewer.screens

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xperiencelabs.modelviewer.R
import com.xperiencelabs.modelviewer.databinding.ItemLayoutBinding


class VariantsAdapter(
    private val context: Context,
    private val variants:Array<String>,
    private val onClick:(position:Int)->Unit,
):RecyclerView.Adapter<VariantsAdapter.ViewHolder>(){

    private var selectedItemPosition:Int = 0
    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var binding = ItemLayoutBinding.bind(itemView)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(position!=RecyclerView.NO_POSITION){
                    onClick(position)

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return  variants.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = variants[position]
        holder.binding.variant.text = item

    }
}