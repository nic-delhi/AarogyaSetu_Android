package nic.goi.aarogyasetu.models.network;

public class ValidateOTP {
    private String primaryId;
    private String passcode;

    public ValidateOTP(String primaryId, String passcode) {
        this.primaryId = primaryId;
        this.passcode = passcode;
    }
}
