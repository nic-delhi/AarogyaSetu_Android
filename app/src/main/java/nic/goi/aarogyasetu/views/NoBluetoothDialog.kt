package nic.goi.aarogyasetu.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_no_bluetooth.*
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.LocalizationUtil


class NoBluetoothDialog : DialogFragment() {

    private var listener: BluetoothActionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_no_bluetooth, container, false)
    }

    interface BluetoothActionListener {
        fun onBluetoothRequested()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NoNetworkDialog.Retry) {
            listener = context as BluetoothActionListener
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        textView2.text = LocalizationUtil.getLocalisedString(context, R.string.turn_on_bluetooth_all_times)
        view.findViewById<View>(R.id.turn_on)
            .setOnClickListener {
                listener?.onBluetoothRequested()
                dismissAllowingStateLoss()

            }

        view.findViewById<View>(R.id.close)
            .setOnClickListener {
                activity?.finishAndRemoveTask()
                dismissAllowingStateLoss()
            }
    }
}