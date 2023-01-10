package com.iarigo.water.ui.fragment_weight

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.water.R
import com.iarigo.water.storage.entity.Weight
import java.util.*
import kotlin.collections.ArrayList

class WeightAdapter (private var myDataSet: ArrayList<Weight>, private var context: Context) :
    RecyclerView.Adapter<WeightAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // each data item is just a string in this case
        val itemTime: TextView = view.findViewById(R.id.time) // time
        val itemWeight: TextView = view.findViewById(R.id.weight) // weight
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // create a new view
        val v: View = LayoutInflater.from(context)
            .inflate(R.layout.list_weight, parent, false) // item view
        return ViewHolder(v as LinearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    // Item view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val map = myDataSet[position]
        setView(holder, map)
    }

    /**
     * Item view element
     */
    private fun setView(holder: ViewHolder, map: Weight) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = map.createAt

        val weight: String = java.lang.String.format(Locale.US, "%.02f", map.weight)

        holder.itemTime.text = context.getString(
            R.string.weight_list_date, calendar.get(Calendar.DAY_OF_MONTH).toString(), (calendar.get(
                Calendar.MONTH) + 1).toString(), calendar.get(Calendar.YEAR).toString()
        )
        holder.itemWeight.text =
            context.getString(R.string.weight_current_value, weight)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return myDataSet.size
    }
}