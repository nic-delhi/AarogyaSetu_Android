package nic.goi.aarogyasetu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nic.goi.aarogyasetu.R;
import nic.goi.aarogyasetu.models.LanguageDTO;


/**
 * Created by Kshitij Khatri on 21/03/20.
 */
public class SelectLanguageAdapter extends RecyclerView.Adapter<SelectLanguageAdapter.LanguageViewHolder> {

    private final ItemClickListener mListener;

    private int mIsSelected = -1;

    private List<LanguageDTO> mLanguageList;

    public SelectLanguageAdapter(int selectedLanaguagePosition, List<LanguageDTO> languageList, ItemClickListener listener) {
        if (selectedLanaguagePosition > -1) {
            mIsSelected = selectedLanaguagePosition;
        }
        mLanguageList = languageList;
        mListener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_language, parent, false);
        return new LanguageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        holder.mLanguage.setText(mLanguageList.get(position).getLanguageTitle());
        holder.mLanguage.setTextColor(position == mIsSelected ? ContextCompat.getColor(holder.itemView.getContext(), R.color.blue) :
                ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
        if (position == mLanguageList.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mLanguageList == null ? 0 : mLanguageList.size();
    }

    class LanguageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mLanguage;
        View divider;

        LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            mLanguage = itemView.findViewById(R.id.tv_language);
            divider = itemView.findViewById(R.id.divider);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int pos = getLayoutPosition();
            if (mListener != null && pos != -1 && pos < mLanguageList.size()) {
                mIsSelected = pos;
                mListener.onItemClick(pos, mLanguageList.get(pos).getLanguageTitle());
            }
        }
    }

    public interface ItemClickListener {
        void onItemClick(int position, String language);
    }
}
