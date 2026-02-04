package nic.goi.aarogyasetu.views.sync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.databinding.DialogSyncDataBinding
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.LocalizationUtil


class SyncDataDialog : DialogFragment() {
    private var listener: SyncDataModeListener? = null
    private lateinit var binding: DialogSyncDataBinding

    interface SyncDataModeListener {
        fun syncDataWith(mode: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SyncDataModeListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_sync_data, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSyncDataDetail.text =
            LocalizationUtil.getLocalisedString(context, R.string.sync_data_detail)
        binding.btnBeingTested.text =
            LocalizationUtil.getLocalisedString(context, R.string.sample_collected_for_testing)
        binding.btnBeingTested.setOnClickListener {
            listener?.syncDataWith(Constants.UPLOAD_TYPES.BEING_TESTED)
            dismissAllowingStateLoss()
        }
        binding.btnTestedPositive.text =
            LocalizationUtil.getLocalisedString(context, R.string.tested_positive)
        binding.btnTestedPositive.setOnClickListener {
            listener?.syncDataWith(Constants.UPLOAD_TYPES.TESTED_POSITIVE_CONSENT)
            dismissAllowingStateLoss()
        }
        binding.close.setOnClickListener { dismissAllowingStateLoss() }
    }
}