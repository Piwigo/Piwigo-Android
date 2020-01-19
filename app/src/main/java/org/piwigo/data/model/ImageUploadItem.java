/*
 * Piwigo for Android
 * Copyright (C) 2016-2019 Piwigo Team http://piwigo.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.piwigo.data.model;

import android.net.Uri;

public class ImageUploadItem
{
    private String imageData;
    private Uri imageUri;
    private String imageName;

    public ImageUploadItem() {}

    public void setImageData(String imageData)
    {
        this.imageData = imageData;
    }

    public void setImageUri(Uri imageUri)
    {
        this.imageUri = imageUri;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    public String getImageData() {
        return imageData;
    }

    public String getImageName() {
        return imageName;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
