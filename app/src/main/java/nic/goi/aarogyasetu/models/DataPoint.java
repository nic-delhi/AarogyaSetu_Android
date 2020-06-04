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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataPoint {

    @SerializedName("ts")
    @Expose
    private String ts;
    @SerializedName("l")
    @Expose
    private LocationObject locationObject;
    @SerializedName("dl")
    @Expose
    private List<Dl> dl = null;


    public DataPoint(BluetoothData postData, String decLatitude, String decLongitude) {
        this.ts = String.valueOf(postData.getTimeStamp());
        this.locationObject = new LocationObject(decLatitude, decLongitude);
        dl = new ArrayList<>();
        dl.add(new Dl(postData.getBluetoothMacAddress(), postData.getDistance(), postData.getTxPowerLevel(), postData.getTxPower()));
    }

}