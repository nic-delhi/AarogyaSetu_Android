package nic.goi.aarogyasetu.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.AuthUtility
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString
import kotlinx.android.synthetic.main.fragment_forth_on_board_intro.*
import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.background.BluetoothScanningService

private const val ARG_PARAM1 = "param1"

class ForthOnBoardIntroFragment : Fragment() {

    private var isRegistrationFlow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isRegistrationFlow = arguments?.getBoolean(ARG_PARAM1) ?: isRegistrationFlow
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forth_on_board_intro, container, false)

        val tvWithCowin20YouCan: TextView =
            view.findViewById(R.id.tv_with_cowin_20_you_can) as TextView
        tvWithCowin20YouCan.text =
            HtmlCompat.fromHtml(
                getLocalisedString(view.context, R.string.with_cowin_20_you_can),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )

        val btnRegister: Button = view.findViewById(R.id.btn_register) as Button
        btnRegister.text = getLocalisedString(view.context, R.string.register_now)
        if (!isRegistrationFlow) {
            btnRegister.visibility = View.INVISIBLE
        }
        if (AuthUtility.isSignedIn())
            btnRegister.text = getString(R.string.next)

        configureButtonClick(btnRegister)

        return view
    }

    private fun configureButtonClick(btnRegister: Button) {
        btnRegister.setOnClickListener {
            if (CorUtility.isNetworkAvailable(context)) {
                if (AuthUtility.isSignedIn() && BluetoothScanningService.serviceRunning) {
                    CorUtility.openWebView(
                        BuildConfig.WEB_URL,
                        "Home",
                        activity!!
                    )
                } else {
                    activity?.startActivity(Intent(activity, PermissionActivity::class.java))
                    activity?.finish()
                }
            } else {
                Toast.makeText(
                    context,
                    getLocalisedString(context, R.string.error_network_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(this).load(R.drawable.onboarding_four).into(imageView20)
        Glide.with(this).load(R.drawable.map_pins).into(imageView2)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param registerationFlow Parameter 1.
         * @return A new instance of fragment ForthOnBoardIntroFragment.
         */
        @JvmStatic
        fun newInstance(registerationFlow: Boolean) =
            ForthOnBoardIntroFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, registerationFlow)
                }
            }
    }
}
