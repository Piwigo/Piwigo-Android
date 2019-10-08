package org.piwigo.io.model;

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
