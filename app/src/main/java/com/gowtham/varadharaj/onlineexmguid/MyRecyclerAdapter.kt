package com.gowtham.varadharaj.onlineexmguid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerAdapter(private val questionList:ArrayList<Db>):RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>() {
    private val TAG = "MyRecyclerAdapter"

    private lateinit var mListener:OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener =listener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =LayoutInflater.from(parent.context).inflate(R.layout.question_item,parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = questionList[position]
        holder.answer.text = currentItem.answer
        holder.question.text = currentItem.question
        holder.questionNumber.text = currentItem.questionNumber

    }

    override fun getItemCount(): Int {
        return questionList.size
    }

    class MyViewHolder(itemView:View,listener: OnItemClickListener):RecyclerView.ViewHolder(itemView){

        val question :TextView = itemView.findViewById(R.id.question)
        val answer :TextView = itemView.findViewById(R.id.answer)
        val questionNumber:TextView = itemView.findViewById(R.id.question_number)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(position = adapterPosition)
            }
        }
    }
}