package com.hfad.weatherapp

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DaysWeatherAdapter(private val dataList: MutableList<DaysWeatherList>) :
    RecyclerView.Adapter<DaysWeatherAdapter.ViewHolder>() {
//    lateinit var context: Context
//    var dataList: MutableList<DaysWeatherList> = mutableListOf()
//    init {
//        this.context = context
//        this.dataList = this.dataList
//    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DaysWeatherAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.days_weather, parent, false) as CardView
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: DaysWeatherAdapter.ViewHolder, position: Int) {
        val daysWeatherList: DaysWeatherList = dataList[position]
        val cardView: CardView = holder.cardView_days_weather
        holder.date_days.text = daysWeatherList.date.toString()
        val drawable: Drawable? = ContextCompat.getDrawable(cardView.context, dataList.get(position).icon_Id)
        holder.icon_days.setImageDrawable(drawable)
        holder.icon_days.contentDescription = daysWeatherList.icon_desc
//        holder.temp_days.text = Resources.getSystem().getString(
//            R.string.days_weather_temp_delimiter, daysWeatherList.temp_max, daysWeatherList.temp_min)
        holder.temp_days.text = daysWeatherList.temp_max.toString()
    }

    override fun getItemCount(): Int {
        return 5
    }

    class ViewHolder(itemView: CardView) : RecyclerView.ViewHolder(itemView) {
        var cardView_days_weather: CardView
        var date_days: TextView
        var icon_days: ImageView
        var temp_days: TextView

        init {
            cardView_days_weather = itemView.findViewById(R.id.card_view_days_weather)
            date_days = itemView.findViewById(R.id.date_day)
            icon_days = itemView.findViewById(R.id.icon_weather)
            temp_days = itemView.findViewById(R.id.day_temp)
        }
    }


}