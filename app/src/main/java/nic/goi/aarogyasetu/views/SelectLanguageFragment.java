package nic.goi.aarogyasetu.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import nic.goi.aarogyasetu.R;
import nic.goi.aarogyasetu.adapters.SelectLanguageAdapter;
import nic.goi.aarogyasetu.analytics.EventNames;
import nic.goi.aarogyasetu.models.LanguageDTO;
import nic.goi.aarogyasetu.prefs.SharedPref;
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants;
import nic.goi.aarogyasetu.utility.AnalyticsUtils;
import nic.goi.aarogyasetu.utility.Constants;
import nic.goi.aarogyasetu.utility.LanguageList;
import nic.goi.aarogyasetu.utility.LocalizationUtil;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Updated by Niharika
 */
public class SelectLanguageFragment extends BottomSheetDialogFragment {

    private SelectLanguageAdapter mAdapter;
    private Button mNext;
    private static final double DIALOG_HEIGHT_RATIO = 1;
    private int mSelectedLanguagePosition = -1;
    private LanguageChangeListener mLanguageChangeListener;
    private static final String TAG = "LanguageSelection";

    interface LanguageChangeListener {
        void languageChange();
    }

    public static void showDialog(FragmentManager fragmentManager, boolean isCancellable) {
        final SelectLanguageFragment fragment = new SelectLanguageFragment();
        fragment.setCancelable(isCancellable);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public SelectLanguageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LanguageChangeListener) {
            this.mLanguageChangeListener = (LanguageChangeListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NotNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_select_language, null);
        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(false);
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        DisplayMetrics displayMetrics = Objects.requireNonNull(getActivity()).getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        int maxHeight = (int) (height * DIALOG_HEIGHT_RATIO);
        bottomSheetBehavior.setPeekHeight(maxHeight);
        initViews(contentView);
    }

    private void initViews(View view) {
        mNext = view.findViewById(R.id.btn_next);
        mNext.setText(LocalizationUtil.getLocalisedString(getContext(), R.string.next));
        List<LanguageDTO> languageList = LanguageList.getLanguageList();

        onNextClick(languageList);
        configureAdapter(view, languageList);

        AnalyticsUtils.sendEvent(EventNames.EVENT_OPEN_LANGUAGE);
    }

    private void onNextClick(List<LanguageDTO> languageList) {
        mNext.setOnClickListener(v -> {
            if (mSelectedLanguagePosition >= 0 && mNext.isEnabled()) {
                String languageCode = SharedPref.getStringParams(getContext(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, Constants.EMPTY);
                if (languageCode.equalsIgnoreCase(languageList.get(mSelectedLanguagePosition).getLanguageCode())) {
                    dismissAllowingStateLoss();
                } else {
                    // Setting the user's choice of language in the shared prefs to fetch the language specific strings from the firebase and store them in shared prefs
                    SharedPref.setStringParams(getContext(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                            languageList.get(mSelectedLanguagePosition).getLanguageCode());
                    AnalyticsUtils.updateUserTraits();
                    if (mLanguageChangeListener != null) {
                        mLanguageChangeListener.languageChange();
                        dismissAllowingStateLoss();
                    }
                }
            } else
                Toast.makeText(getContext(), R.string.please_select_a_language_to_proceed, Toast.LENGTH_SHORT).show();
        });
    }

    private void configureAdapter(View view, List<LanguageDTO> languageList) {
        configureSelectedPosition(languageList);
        RecyclerView recyclerView = view.findViewById(R.id.rv_select_language);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new SelectLanguageAdapter(mSelectedLanguagePosition, languageList, (position, language) -> {
            mSelectedLanguagePosition = position;
            mNext.setEnabled(true);
            mAdapter.notifyDataSetChanged();
        });
        recyclerView.setAdapter(mAdapter);
    }

    private void configureSelectedPosition(List<LanguageDTO> languageList) {
        String languageCode = SharedPref.getStringParams(getContext(), SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE, "en");
        if (!TextUtils.isEmpty(languageCode)) {
            for (int index = 0; index < languageList.size(); index++) {
                if (languageList.get(index).getLanguageCode() != null && languageList.get(index).getLanguageCode().equalsIgnoreCase(languageCode)) {
                    mSelectedLanguagePosition = index;
                    break;
                }
            }
        }
    }
}
