package nic.goi.aarogyasetu.models.rootdetection;

import androidx.annotation.NonNull;



import java.util.List;

import nic.goi.aarogyasetu.annotation.CheckingState;


public class TotalResult {
    @NonNull
    private final List<CheckInfo> mList;

    @CheckingState
    private final int mCheckState;

    public TotalResult(@NonNull List<CheckInfo> list, @CheckingState int checkState) {
        mList = list;
        mCheckState = checkState;
    }

    @NonNull
    public List<CheckInfo> getList() {
        return mList;
    }

    @CheckingState
    public int getCheckState() {
        return mCheckState;
    }
}
