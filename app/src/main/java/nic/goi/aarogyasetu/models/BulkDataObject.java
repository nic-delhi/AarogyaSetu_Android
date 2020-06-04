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

import java.util.List;

import nic.goi.aarogyasetu.utility.Constants;

public class BulkDataObject {
    @SerializedName("d")
    @Expose
    private String d;

    @SerializedName(Constants.UPLOAD_TYPE)
    @Expose
    private String uploadType;

    @SerializedName("data")
    @Expose
    private List<DataPoint> data;

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public List<DataPoint> getData() {
        return data;
    }

    public void setData(List<DataPoint> data) {
        this.data = data;
    }

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String type) {
        this.uploadType = type;
    }
}
