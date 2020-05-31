package nic.goi.aarogyasetu.utility.rootdetection;


import nic.goi.aarogyasetu.models.rootdetection.TotalResult;

public interface IChecksResultListener {
    void onProcessStarted();
    void onUpdateResult(TotalResult result);
    void onProcessFinished(TotalResult result);
}
