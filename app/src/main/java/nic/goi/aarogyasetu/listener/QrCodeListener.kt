package nic.goi.aarogyasetu.listener

/**
 * @author Niharika.Arora
 */
interface QrCodeListener {
    fun onQrCodeFetched(text: String)

    fun onFailure()
}
