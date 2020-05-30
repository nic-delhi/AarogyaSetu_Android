package nic.goi.aarogyasetu.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.adapters.SelectLanguageAdapter
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.models.LanguageDTO
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import nic.goi.aarogyasetu.utility.AnalyticsUtils
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.LanguageList
import nic.goi.aarogyasetu.utility.LocalizationUtil

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Objects

/**
 * A simple [Fragment] subclass.
 * Updated by Niharika
 */
class SelectLanguageFragment : BottomSheetDialogFragment() {

    private var mAdapter: SelectLanguageAdapter? = null
    private var mNext: Button? = null
    private var mSelectedLanguagePosition = -1
    private var mLanguageChangeListener: LanguageChangeListener? = null

    internal interface LanguageChangeListener {
        fun languageChange()
    }

    @Override
    fun onAttach(@NonNull context: Context) {
        super.onAttach(context)
        if (context is LanguageChangeListener) {
            this.mLanguageChangeListener = context
        }
    }

    @Override
    fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("RestrictedApi")
    @Override
    fun setupDialog(@NotNull dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(getContext(), R.layout.fragment_select_language, null)
        dialog.setContentView(contentView)
        dialog.setCanceledOnTouchOutside(false)
        val bottomSheetBehavior = BottomSheetBehavior.from(contentView.getParent() as View)
        val displayMetrics = Objects.requireNonNull(getActivity()).getResources().getDisplayMetrics()
        val height = displayMetrics.heightPixels
        val maxHeight = (height * DIALOG_HEIGHT_RATIO).toInt()
        bottomSheetBehavior.setPeekHeight(maxHeight)
        initViews(contentView)
    }

    private fun initViews(view: View) {
        mNext = view.findViewById(R.id.btn_next)
        mNext!!.setText(LocalizationUtil.getLocalisedString(getContext(), R.string.next))
        val languageList = LanguageList.getLanguageList()

        onNextClick(languageList)
        configureAdapter(view, languageList)

        AnalyticsUtils.sendEvent(EventNames.EVENT_OPEN_LANGUAGE)
    }

    private fun onNextClick(languageList: List<LanguageDTO>) {
        mNext!!.setOnClickListener({ v ->
            if (mSelectedLanguagePosition >= 0 && mNext!!.isEnabled()) {
                val languageCode = SharedPref.getStringParams(
                    getContext(),
                    SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                    Constants.EMPTY
                )
                if (languageCode.equalsIgnoreCase(languageList[mSelectedLanguagePosition].getLanguageCode())) {
                    dismissAllowingStateLoss()
                } else {
                    // Setting the user's choice of language in the shared prefs to fetch the language specific strings from the firebase and store them in shared prefs
                    SharedPref.setStringParams(
                        getContext(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                        languageList[mSelectedLanguagePosition].getLanguageCode()
                    )
                    AnalyticsUtils.updateUserTraits()
                    if (mLanguageChangeListener != null) {
                        mLanguageChangeListener!!.languageChange()
                        dismissAllowingStateLoss()
                    }
                }
            } else
                Toast.makeText(getContext(), R.string.please_select_a_language_to_proceed, Toast.LENGTH_SHORT).show()
        })
    }

    private fun configureAdapter(view: View, languageList: List<LanguageDTO>) {
        configureSelectedPosition(languageList)
        val recyclerView = view.findViewById(R.id.rv_select_language)
        recyclerView.setLayoutManager(LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false))
        mAdapter = SelectLanguageAdapter(mSelectedLanguagePosition, languageList) { position, language ->
            mSelectedLanguagePosition = position.toInt()
            mNext!!.setEnabled(true)
            mAdapter!!.notifyDataSetChanged()
        }
        recyclerView.setAdapter(mAdapter)
    }

    private fun configureSelectedPosition(languageList: List<LanguageDTO>) {
        val languageCode =
            SharedPref.getStringParams(getContext(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, "en")
        if (!TextUtils.isEmpty(languageCode)) {
            for (index in 0 until languageList.size()) {
                if (languageList[index].getLanguageCode() != null && languageList[index].getLanguageCode().equalsIgnoreCase(
                        languageCode
                    )
                ) {
                    mSelectedLanguagePosition = index
                    break
                }
            }
        }
    }

    companion object {
        private val DIALOG_HEIGHT_RATIO = 1.0
        private val TAG = "LanguageSelection"

        fun showDialog(fragmentManager: FragmentManager, isCancellable: Boolean) {
            val fragment = SelectLanguageFragment()
            fragment.setCancelable(isCancellable)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(fragment, TAG)
            fragmentTransaction.commitAllowingStateLoss()
        }
    }
}// Required empty public constructor
