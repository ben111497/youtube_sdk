package com.example.youtubesdk


import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.red

class MyListAdapter(private val context: Activity, private val caption: Array<String?>,private val choose:Int): ArrayAdapter<String>(context, R.layout.custom_list,caption) {
    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.custom_list, null, true)

        val caption2 = rowView.findViewById(R.id.caption) as TextView
        val number = rowView.findViewById(R.id.number) as TextView
        val aaa=rowView.findViewById(R.id.aaa) as LinearLayout
        var no = 0

        for (i in 0 until position + 1) {
            no++
            caption2.text = "${caption[i]}"
            number.text = "${no}"
            if(choose==i){
                aaa.setBackgroundColor(Color.argb(255,227,224,221))
            }else{
                aaa.setBackgroundColor(Color.alpha(255))
            }
        }
        return rowView
    }

}