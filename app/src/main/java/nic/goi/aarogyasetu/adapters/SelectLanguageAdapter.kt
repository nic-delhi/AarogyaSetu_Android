package nic.goi.aarogyasetu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.models.LanguageDTO


/**
 * Created by Kshitij Khatri on 21/03/20.
 */
class SelectLanguageAdapter(
    selectedLanaguagePosition: Int,
    private val mLanguageList: List<LanguageDTO>?,
    private val mListener: ItemClickListener?
) : RecyclerView.Adapter<SelectLanguageAdapter.LanguageViewHolder>() {

    private var mIsSelected = -1

    val itemCount: Int
        @Override
        get() = mLanguageList?.size() ?: 0

    init {
        if (selectedLanaguagePosition > -1) {
            mIsSelected = selectedLanaguagePosition
        }
    }

    @NonNull
    @Override
    fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_language, parent, false)
        return LanguageViewHolder(view)

    }

    @Override
    fun onBindViewHolder(@NonNull holder: LanguageViewHolder, position: Int) {
        holder.mLanguage.setText(mLanguageList!![position].getLanguageTitle())
        holder.mLanguage.setTextColor(
            if (position == mIsSelected)
                ContextCompat.getColor(holder.itemView.getContext(), R.color.blue)
            else
                ContextCompat.getColor(holder.itemView.getContext(), R.color.black)
        )
        if (position == mLanguageList.size() - 1) {
            holder.divider.setVisibility(View.GONE)
        } else {
            holder.divider.setVisibility(View.VISIBLE)
        }
    }

    internal inner class LanguageViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var mLanguage: TextView
        var divider: View

        init {
            mLanguage = itemView.findViewById(R.id.tv_language)
            divider = itemView.findViewById(R.id.divider)
            itemView.setOnClickListener(this)

        }

        @Override
        fun onClick(v: View) {
            val pos = getLayoutPosition()
            if (mListener != null && pos != -1 && pos < mLanguageList!!.size()) {
                mIsSelected = pos
                mListener.onItemClick(pos, mLanguageList!![pos].getLanguageTitle())
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, language: String)
    }
}
