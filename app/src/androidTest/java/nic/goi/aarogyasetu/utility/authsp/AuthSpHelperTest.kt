package nic.goi.aarogyasetu.utility.authsp

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthSpHelperTest {
    private lateinit var authSpHelper: AuthSpHelper
    private val key = "test_key"
    private val value = "test_value"
    private val defaultValue: String? = null

    @Before
    fun setup() {
        authSpHelper = AuthSpFactory.instance
    }

    @Test
    fun testGetString_NotFound() {
        val found = authSpHelper.getString(key, defaultValue)

        Assert.assertThat(found, Matchers.equalTo(defaultValue))
    }

    @Test
    fun testGetString_Found() {
        authSpHelper.putString(key, value)

        Assert.assertThat(authSpHelper.getString(key, defaultValue), Matchers.equalTo(value))
    }

    @Test
    fun testDeleteString() {
        authSpHelper.putString(key, value)
        Assert.assertThat(authSpHelper.getString(key, defaultValue), Matchers.equalTo(value))
        authSpHelper.removeKey(key)

        Assert.assertThat(authSpHelper.getString(key, defaultValue), Matchers.equalTo(defaultValue))
    }
}