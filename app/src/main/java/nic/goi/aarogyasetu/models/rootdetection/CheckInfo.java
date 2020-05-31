package nic.goi.aarogyasetu.models.rootdetection;

import androidx.annotation.Nullable;

import nic.goi.aarogyasetu.annotation.CheckingMethodType;


public class CheckInfo {

    @Nullable
    private Boolean mState;

    @CheckingMethodType
    private int mTypeCheck;

    public CheckInfo(@Nullable Boolean state, @CheckingMethodType int typeCheck) {
        this.mState = state;
        this.mTypeCheck = typeCheck;
    }

    @Nullable
    public Boolean getState() {
        return mState;
    }

    public void setState(@Nullable Boolean state) {
        this.mState = state;
    }

    @CheckingMethodType
    public int getTypeCheck() {
        return mTypeCheck;
    }

    public void setTypeCheck(int typeCheck) {
        this.mTypeCheck = typeCheck;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckInfo)) return false;

        CheckInfo checkInfo = (CheckInfo) o;

        if (mTypeCheck != checkInfo.mTypeCheck) return false;
        return mState != null ? mState.equals(checkInfo.mState) : checkInfo.mState == null;

    }

    @Override
    public int hashCode() {
        int result = mState != null ? mState.hashCode() : 0;
        result = 31 * result + mTypeCheck;
        return result;
    }
}
