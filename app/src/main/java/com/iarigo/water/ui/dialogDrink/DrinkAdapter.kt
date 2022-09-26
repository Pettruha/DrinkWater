package com.iarigo.water.ui.dialogDrink

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.water.R
import java.util.*
import kotlin.collections.ArrayList

class DrinkAdapter (private var myDataSet: ArrayList<HashMap<String, String>>, private var context: Context, private val unitClickListener: OnItemClickListener) :
    RecyclerView.Adapter<DrinkAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: HashMap<String, String>, position: Int)
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // each data item is just a string in this case
        val itemName: TextView = view.findViewById(R.id.name) // название
        val itemPercent: TextView = view.findViewById(R.id.percent) // процент воды
        val itemLinearLayout: LinearLayout = view.findViewById(R.id.list_drink_item_content)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // create a new view
        val v: View = LayoutInflater.from(context)
            .inflate(R.layout.list_drink, parent, false) // внешний вид элемента списка
        return ViewHolder(v as LinearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    // Внешний вид элемента
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val map = myDataSet[position]
        setView(holder, map) // внешний вид. что показывать,что нет
        // клик по элементу
        holder.itemView.setOnClickListener{
            unitClickListener.onItemClick(map, position)
        }
    }

    /**
     * Определяем внешний вид элемента
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setView(holder: ViewHolder, map: HashMap<String, String>) {
        holder.itemName.text = map["name"] // название измерения
        holder.itemPercent.text = context.getString(R.string.list_drink_percent, map["percent"])
        if (map["selected"] == "1") {
            holder.itemLinearLayout.background = context.getDrawable(R.color.drink_list_selected)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return myDataSet.size
    }
}