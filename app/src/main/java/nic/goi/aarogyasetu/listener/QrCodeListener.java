package nic.goi.aarogyasetu.listener;

/**
 * @author Niharika.Arora
 */
public interface QrCodeListener {
    void onQrCodeFetched(String text);

    void onFailure();
}
