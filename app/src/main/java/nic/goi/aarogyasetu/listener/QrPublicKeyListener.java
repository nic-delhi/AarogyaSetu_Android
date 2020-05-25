package nic.goi.aarogyasetu.listener;

/**
 * @author Niharika.Arora
 */
public interface QrPublicKeyListener {
    void onQrPublicKeyFetched(String text);

    void onPublicKeyFetchFailure();
}
