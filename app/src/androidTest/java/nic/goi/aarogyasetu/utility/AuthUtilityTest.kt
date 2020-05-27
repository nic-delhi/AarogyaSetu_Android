/*
 * Copyright 2020 Government of India
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nic.goi.aarogyasetu.utility

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthUtilityTest {
    private val token = "token#123"

    @Test
    fun testGetToken() {
        AuthUtility.setToken(token)

        Assert.assertThat(AuthUtility.getToken(), Matchers.equalTo(token))
    }

    @Test
    fun testGetRefreshToken() {
        AuthUtility.setRefreshToken(token)

        Assert.assertThat(AuthUtility.getRefreshToken(), Matchers.equalTo(token))
    }

    @Test
    fun testGetUserName() {
        AuthUtility.setUserName(token)

        Assert.assertThat(AuthUtility.getUserName(), Matchers.equalTo(token))
    }

    @Test
    fun testRemoveToken() {
        AuthUtility.setToken(token)
        Assert.assertThat(AuthUtility.getToken(), Matchers.equalTo(token))
        AuthUtility.removeToken()

        Assert.assertThat(AuthUtility.getToken(), Matchers.equalTo(""))
    }

    @Test
    fun testRemoveRefreshToken() {
        AuthUtility.setRefreshToken(token)
        Assert.assertThat(AuthUtility.getRefreshToken(), Matchers.equalTo(token))
        AuthUtility.removeRefreshToken()

        Assert.assertThat(AuthUtility.getRefreshToken(), Matchers.equalTo(""))
    }

    @Test
    fun testRemoveUserName() {
        AuthUtility.setUserName(token)
        Assert.assertThat(AuthUtility.getUserName(), Matchers.equalTo(token))
        AuthUtility.removeUserName()

        Assert.assertThat(AuthUtility.getUserName(), Matchers.equalTo(""))
    }

    @Test
    fun testClearUserDetails() {
        AuthUtility.setToken(token)
        AuthUtility.setRefreshToken(token)
        AuthUtility.setUserName(token)
        Assert.assertThat(AuthUtility.getToken(), Matchers.equalTo(token))
        Assert.assertThat(AuthUtility.getRefreshToken(), Matchers.equalTo(token))
        Assert.assertThat(AuthUtility.getUserName(), Matchers.equalTo(token))
        AuthUtility.clearUserDetails()

        Assert.assertThat(AuthUtility.getToken(), Matchers.equalTo(""))
        Assert.assertThat(AuthUtility.getRefreshToken(), Matchers.equalTo(""))
        Assert.assertThat(AuthUtility.getUserName(), Matchers.equalTo(""))
    }

    @Test
    fun testIsSignedIn_NotSignedIn() {
        AuthUtility.removeToken()

        Assert.assertThat(AuthUtility.isSignedIn(), Matchers.equalTo(false))
    }

    @Test
    fun testIsSignedIn_SignedIn() {
        AuthUtility.setToken(token)
        Assert.assertThat(AuthUtility.getToken(), Matchers.equalTo(token))

        Assert.assertThat(AuthUtility.isSignedIn(), Matchers.equalTo(true))
    }
}