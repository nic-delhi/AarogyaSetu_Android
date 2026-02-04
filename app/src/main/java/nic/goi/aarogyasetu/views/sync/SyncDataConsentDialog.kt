package nic.goi.aarogyasetu.views.sync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.analytics.EventParams
import nic.goi.aarogyasetu.databinding.DialogSyncDataConsentBinding
import nic.goi.aarogyasetu.utility.AnalyticsUtils.sendEvent
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.LocalizationUtil


class SyncDataConsentDialog : DialogFragment() {
    private var listener: ConfirmationListener? = null
    private var uploadType: String? = null
    private lateinit var binding: DialogSyncDataConsentBinding

    interface ConfirmationListener {
        fun proceedSyncing(uploadType: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert)

        uploadType = arguments?.getString(Constants.UPLOAD_TYPE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConfirmationListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_sync_data_consent, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSyncConsentDetail.text =
            LocalizationUtil.getLocalisedString(context, R.string.sync_data_user_consent)
        binding.btnConfirmAndProceed.text =
            LocalizationUtil.getLocalisedString(context, R.string.confirm_and_proceed)
        binding.btnConfirmAndProceed.setOnClickListener {
            listener?.proceedSyncing(uploadType ?: Constants.UPLOAD_TYPES.BEING_TESTED)
            val bundle = Bundle()
            bundle.putString(EventParams.PROP_UPLOAD_TYPE, uploadType)
            sendEvent(EventNames.EVENT_SUBMIT_UPLOAD_CONSENT, bundle)
            dismissAllowingStateLoss()
        }
        binding.close.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(EventParams.PROP_UPLOAD_TYPE, uploadType)
            sendEvent(EventNames.EVENT_CONSENT_CANCELLED, bundle)
            dismissAllowingStateLoss()
        }


        val bundle = Bundle()
        bundle.putString(EventParams.PROP_UPLOAD_TYPE, uploadType)
        sendEvent(EventNames.EVENT_OPEN_UPLOAD_CONSENT_SCREEN, bundle)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param uploadType Type selected or from notification.
         * @return A new instance of fragment SyncDataConsentDialog.
         */
        @JvmStatic
        fun newInstance(uploadType: String) =
            SyncDataConsentDialog().apply {
                arguments = Bundle().apply {
                    putString(Constants.UPLOAD_TYPE, uploadType)
                }
            }
    }
}