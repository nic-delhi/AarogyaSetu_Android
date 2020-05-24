package nic.goi.aarogyasetu.utility

import androidx.test.ext.junit.runners.AndroidJUnit4
import nic.goi.aarogyasetu.models.EncryptedInfo
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureUtilTest {
    private lateinit var encryptionUtil: EncryptionUtil
    private lateinit var decryptionUtil: DecryptionUtil
    private val data = "test_data"

    @Before
    fun setup() {
        encryptionUtil = EncryptionUtil()
        decryptionUtil = DecryptionUtil()
    }

    @Test
    fun testEncryptText() {
        val encryptedData = encryptionUtil.encryptText(data)
        val encryptedInfo = EncryptedInfo().apply {
            data = encryptedData
            iv = encryptionUtil.iv
        }

        Assert.assertThat(decryptionUtil.decryptData(encryptedInfo), Matchers.equalTo(data))
    }

    @Test
    fun testEncrypt() {
        val encryptedInfo = encryptionUtil.encrypt(data)

        Assert.assertThat(decryptionUtil.decryptData(encryptedInfo), Matchers.equalTo(data))
    }
}