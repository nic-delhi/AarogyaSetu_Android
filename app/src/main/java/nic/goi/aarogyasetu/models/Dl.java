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

package nic.goi.aarogyasetu.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Niharika.Arora
 */
public class Dl {

    @SerializedName("d")
    @Expose
    private String d;
    @SerializedName("dist")
    @Expose
    private Integer dist;
    @SerializedName("tx_level")
    @Expose
    private String txLevel;
    @SerializedName("tx_power")
    @Expose
    private String txPower;

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public Dl(String d, Integer dist, String txPowerLevel, String txPower) {
        this.d = d;
        this.dist = dist;
        this.txPower = txPower;
        this.txLevel = txPowerLevel;
    }
}