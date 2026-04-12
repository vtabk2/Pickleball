package com.gs.pickleball.ui.common

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import java.text.Normalizer

class AccentInsensitiveAdapter(
    context: Context,
    private val allItems: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, ArrayList(allItems)) {
    private val normalized = allItems.map { normalize(it) }
    private var filtered: List<String> = allItems

    override fun getCount(): Int = filtered.size

    override fun getItem(position: Int): String = filtered[position]

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = normalize(constraint?.toString().orEmpty())
                filtered = if (query.isBlank()) {
                    allItems
                } else {
                    allItems.filterIndexed { index, _ ->
                        normalized[index].contains(query)
                    }
                }
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filtered = results?.values as? List<String> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private fun normalize(input: String): String {
            val temp = Normalizer.normalize(input, Normalizer.Form.NFD)
            return temp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                .lowercase()
        }
    }
}
