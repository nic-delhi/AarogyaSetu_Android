package nic.goi.aarogyasetu.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment

import java.util.Objects

import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.LocalizationUtil

class NoNetworkDialog : DialogFragment() {

    private var listener: Retry? = null
    internal var retryUrl: String? = null
        set

    internal interface Retry {
        fun retry(url: String?)
    }

    @Override
    fun onAttach(@NonNull context: Context) {
        super.onAttach(context)
        if (context is Retry) {
            listener = context
        }
    }

    @Nullable
    @Override
    fun onCreateView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup, @Nullable savedInstanceState: Bundle): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.dialog_no_network, container, false)
    }

    @Override
    fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle) {
        super.onViewCreated(view, savedInstanceState)

        val noInternetDetail = view.findViewById(R.id.textView2)
        noInternetDetail.setText(
            LocalizationUtil.getLocalisedString(
                getContext(),
                R.string.make_sure_your_phone_is_connected_to_the_wifi_or_switch_to_mobile_data
            )
        )
        view.findViewById(R.id.try_again).setOnClickListener({ v ->
            if (listener != null)
                listener!!.retry(retryUrl)
            dismissAllowingStateLoss()
        })

        view.findViewById(R.id.settings).setOnClickListener(
            { v ->
                Objects.requireNonNull(getActivity())
                    .startActivityForResult(Intent(android.provider.Settings.ACTION_SETTINGS), HomeActivity.NO_NETWORK)
            })
    }
}


