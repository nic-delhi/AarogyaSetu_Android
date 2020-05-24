package nic.goi.aarogyasetu.models;

/**
 * Created by Kshitij Khatri on 21/03/20.
 */
public class LanguageDTO {

    private String mLanguageCode,mLanguageTitle;

    public LanguageDTO(String mLanguageCode, String mLanguageTitle) {
        this.mLanguageCode = mLanguageCode;
        this.mLanguageTitle = mLanguageTitle;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public String getLanguageTitle() {
        return mLanguageTitle;
    }

}
