package com.iarigo.water.ui.fragment_water

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.water.R
import com.iarigo.water.storage.entity.Water
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WaterAdapter (private var myDataSet: ArrayList<Water>, private var context: Context) :
    RecyclerView.Adapter<WaterAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // each data item is just a string in this case
        val itemTime: TextView = view.findViewById(R.id.time) // time
        val itemName: TextView = view.findViewById(R.id.name) // name
        val itemDrinkCount: TextView = view.findViewById(R.id.water_count_drink) // count of water
        val itemWaterCount: TextView = view.findViewById(R.id.water_count_water) // count of drink
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // create a new view
        val v: View = LayoutInflater.from(context)
            .inflate(R.layout.list_water, parent, false) // item view layout
        return ViewHolder(v as LinearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    // Item view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val map = myDataSet[position]
        setView(holder, map) // set item view
    }

    /**
     * Set item view
     */
    private fun setView(holder: ViewHolder, map: Water) {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = map.createAt

        val time: String = formatter.format(calendar.time)

        holder.itemTime.text = context.getString(R.string.main_list_time, time)
        holder.itemName.text = map.drinkName // drink name

        holder.itemDrinkCount.text = context.getString(R.string.main_list_water, map.countDrink.toString())
        holder.itemWaterCount.text = context.getString(R.string.main_list_water, map.countWater.toString())

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return myDataSet.size
    }
}