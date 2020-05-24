package nic.goi.aarogyasetu.utility;

/**
 * @author Niharika.Arora
 */
public interface QrCodeListener {
    void onQrCodeFetched(String text);

    void onFailure();
}
