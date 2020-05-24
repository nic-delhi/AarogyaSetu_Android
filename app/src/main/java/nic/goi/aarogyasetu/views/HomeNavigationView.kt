package nic.goi.aarogyasetu.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.AuthUtility
import nic.goi.aarogyasetu.utility.LocalizationUtil

class HomeNavigationView(context: Context, attrs: AttributeSet) : NavigationView(context, attrs) {
    var view: View? = null

    fun inflate(onClick: OnClickListener) {
        val view = View.inflate(context, R.layout.navigation_layout, this)
        view.findViewById<Button>(R.id.qr).setOnClickListener(onClick)
        view.findViewById<Button>(R.id.verify_installed_app).setOnClickListener(onClick)
        view.findViewById<Button>(R.id.share_data).setOnClickListener(onClick)
        view.findViewById<Button>(R.id.call).setOnClickListener(onClick)
        view.findViewById<TextView>(R.id.faq).setOnClickListener(onClick)
        view.findViewById<TextView>(R.id.privacy_policy).setOnClickListener(onClick)
        view.findViewById<TextView>(R.id.terms).setOnClickListener(onClick)
        this.view = view
    }

    fun setDetail() {
        view?.let { view ->
            val number = view.findViewById<TextView>(R.id.tv_mobile_no)
            number.text = AuthUtility.getMobile()

            val name = view.findViewById<TextView>(R.id.tv_user_name)
            val userName = AuthUtility.getUserName()
            if (!userName.isNullOrEmpty()) {
                name.text = userName
                name.visibility = View.VISIBLE
            } else {
                name.visibility = View.GONE
            }

            view.findViewById<TextView>(R.id.tv_qr).text =
                LocalizationUtil.getLocalisedString(context, R.string.generator_scanner_qr_code)

            view.findViewById<TextView>(R.id.tv_verify_installed_app).text =
                LocalizationUtil.getLocalisedString(context, R.string.verify_installed_app)

            view.findViewById<TextView>(R.id.tv_share_data).text =
                LocalizationUtil.getLocalisedString(context, R.string.share_data_with_govt)
            view.findViewById<TextView>(R.id.tv_share_data_detail).text =
                LocalizationUtil.getLocalisedString(context, R.string.share_data_with_govt_detail)


            view.findViewById<TextView>(R.id.tv_call).text =
                LocalizationUtil.getLocalisedString(context, R.string.call_helpline)
            view.findViewById<TextView>(R.id.tv_call_detail).text =
                LocalizationUtil.getLocalisedString(context, R.string.call_helpline_detail)

            val faq = view.findViewById<TextView>(R.id.faq)
            faq.text = LocalizationUtil.getLocalisedString(context, R.string.faq)

            val privacyPolicy = view.findViewById<TextView>(R.id.privacy_policy)
            privacyPolicy.text =
                LocalizationUtil.getLocalisedString(context, R.string.privacy_policy)

            val terms = view.findViewById<TextView>(R.id.terms)
            terms.text = LocalizationUtil.getLocalisedString(context, R.string.terms_of_use)

            val version = view.findViewById<TextView>(R.id.tv_app_version)
            version.text = LocalizationUtil.getSpannableString(
                context,
                R.string.app_version,
                arrayOf(BuildConfig.VERSION_NAME)
            )
        }
    }

    fun hideShareData() {
        view?.let {
            it.findViewById<ImageView>(R.id.iv_share_data).visibility = View.GONE
            it.findViewById<TextView>(R.id.tv_share_data).visibility = View.GONE
            it.findViewById<TextView>(R.id.tv_share_data_detail).visibility = View.GONE
            it.findViewById<View>(R.id.divider_4).visibility = View.GONE
        }
    }

}