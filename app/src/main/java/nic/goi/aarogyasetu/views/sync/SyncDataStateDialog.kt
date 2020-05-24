package nic.goi.aarogyasetu.views.sync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.analytics.EventParams
import nic.goi.aarogyasetu.utility.AnalyticsUtils.sendEvent
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.LocalizationUtil


class SyncDataStateDialog : DialogFragment() {
    private var listener: SyncListener? = null
    private var state: State = State.SYNCING
    private var uploadType: String? = null

    interface SyncListener {
        fun cancelSyncing()
        fun retrySyncing(uploadType: String)
    }

    enum class State {
        SYNCING, FAILURE, SUCCESS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert)

        state = State.valueOf(arguments?.getString(STATE) ?: State.SYNCING.name)
        uploadType = arguments?.getString(Constants.UPLOAD_TYPE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SyncListener) {
            listener = context
        }
    }

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_sync_data_state, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val stateIcon = view.findViewById<ImageView>(R.id.ic_state)
        val title = view.findViewById<TextView>(R.id.tv_sync_title)
        val detail = view.findViewById<TextView>(R.id.tv_sync_detail)
        val syncBtn = view.findViewById<Button>(R.id.btn_sync)
        when (state) {
            State.SYNCING -> setSyncingMode(stateIcon, title, detail, syncBtn)
            State.SUCCESS -> setSuccessMode(stateIcon, title, detail, syncBtn)
            State.FAILURE -> setFailureMode(stateIcon, title, detail, syncBtn)
        }
        view.findViewById<Button>(R.id.btn_sync).setOnClickListener {
            if (state == State.SYNCING) {
                listener?.cancelSyncing()
            } else if (state == State.FAILURE) {
                uploadType?.let {
                    listener?.retrySyncing(it)
                }
            }
            dismissAllowingStateLoss()
        }
        view.findViewById<ImageView>(R.id.close).setOnClickListener { dismissAllowingStateLoss() }

        val bundle = Bundle()
        bundle.putString(EventParams.PROP_UPLOAD_TYPE, uploadType)
        sendEvent(EventNames.EVENT_OPEN_UPLOAD_CHOICE, bundle)
    }

    private fun setFailureMode(
        stateIcon: ImageView,
        title: TextView,
        detail: TextView,
        syncBtn: Button
    ) {
        stateIcon.setImageResource(R.drawable.ic_sync_failure)
        title.text = LocalizationUtil.getLocalisedString(context, R.string.syncing_data_failed)
        context?.let {
            title.setTextColor(ContextCompat.getColor(it, R.color.sync_failure))
        }
        detail.text =
            LocalizationUtil.getLocalisedString(context, R.string.syncing_data_failed_detail)
        syncBtn.text = LocalizationUtil.getLocalisedString(context, R.string.retry)
    }

    private fun setSuccessMode(
        stateIcon: ImageView,
        title: TextView,
        detail: TextView,
        syncBtn: Button
    ) {
        stateIcon.setImageResource(R.drawable.ic_sync_success)
        title.text = LocalizationUtil.getLocalisedString(context, R.string.syncing_data_success)
        context?.let {
            title.setTextColor(ContextCompat.getColor(it, R.color.sync_success))
        }
        detail.text =
            LocalizationUtil.getLocalisedString(context, R.string.syncing_data_success_detail)
        syncBtn.text = LocalizationUtil.getLocalisedString(context, R.string.ok)
    }

    private fun setSyncingMode(
        stateIcon: ImageView,
        title: TextView,
        detail: TextView,
        syncBtn: Button
    ) {
        stateIcon.setImageResource(R.drawable.ic_syncing)
        val aniRotate: Animation = AnimationUtils.loadAnimation(
            requireContext().applicationContext,
            R.anim.rotate
        )
        stateIcon.startAnimation(aniRotate)
        title.text = LocalizationUtil.getLocalisedString(context, R.string.syncing_data)
        context?.let {
            title.setTextColor(ContextCompat.getColor(it, R.color.black))
        }
        detail.text = LocalizationUtil.getLocalisedString(context, R.string.syncing_data_detail)
        syncBtn.text = LocalizationUtil.getLocalisedString(context, R.string.cancel)
    }

    companion object {
        private const val STATE = "state"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param state Syncing state.
         * @param uploadType Type selected or from notification.
         * @return A new instance of fragment SyncDataStateDialog.
         */
        @JvmStatic
        fun newInstance(state: State, uploadType: String?) =
            SyncDataStateDialog().apply {
                arguments = Bundle().apply {
                    putString(STATE, state.name)
                    putString(Constants.UPLOAD_TYPE, uploadType)
                }
            }
    }
}

