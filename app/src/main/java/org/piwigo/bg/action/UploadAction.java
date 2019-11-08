package org.piwigo.bg.action;

import android.accounts.Account;
import android.net.Uri;

import java.io.Serializable;

public class UploadAction implements Serializable
{
    private String fileName;
    private UploadData uploadData;

    public UploadAction(String fileName)
    {
        this.fileName = fileName;
        this.uploadData = new UploadData();
    }

    public String getFileName()
    {
        return (fileName);
    }

    public UploadData getUploadData() {
        return (uploadData);
    }

    public class UploadData implements Serializable {

        private String imageData;
        private String imageName;
        private String targetUri;
        private int categoryId;

        public void setImageData(String imageData)
        {
            this.imageData = imageData;
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

        public void setTargetUri(Uri targetUri)
        {
            this.targetUri = targetUri.toString();
        }

        public void setCategoryId(int categoryId)
        {
            this.categoryId = categoryId;
        }

        public Uri getTargetUri()
        {
            return (Uri.parse(targetUri));
        }

        public int getCategoryId()
        {
            return (categoryId);
        }
    }
}
