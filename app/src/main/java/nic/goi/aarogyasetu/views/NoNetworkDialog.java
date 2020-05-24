package nic.goi.aarogyasetu.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import nic.goi.aarogyasetu.R;
import nic.goi.aarogyasetu.utility.LocalizationUtil;

public class NoNetworkDialog extends DialogFragment {

    private Retry listener;
    private String retryUrl;

    interface Retry {
        void retry(String url);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Retry) {
            listener = (Retry) context;
        }
    }

    public void setRetryUrl(String retryUrl) {
        this.retryUrl = retryUrl;
    }

    String getRetryUrl() {
        return retryUrl;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.dialog_no_network, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView noInternetDetail = view.findViewById(R.id.textView2);
        noInternetDetail.setText(LocalizationUtil.getLocalisedString(getContext(), R.string.make_sure_your_phone_is_connected_to_the_wifi_or_switch_to_mobile_data));
        view.findViewById(R.id.try_again).setOnClickListener(v -> {
            if (listener != null)
                listener.retry(retryUrl);
            dismissAllowingStateLoss();
        });

        view.findViewById(R.id.settings).setOnClickListener(
                v -> Objects.requireNonNull(getActivity()).startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), HomeActivity.NO_NETWORK));
    }
}


