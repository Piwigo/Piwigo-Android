package org.piwigo.bg;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;

public class ImageUploadQueue<UploadAction> extends AbstractQueue<UploadAction> implements Serializable {

    private LinkedList<UploadAction> images;

    public ImageUploadQueue() {
        this.images = new LinkedList<>();
    }

    @NonNull
    @Override
    public Iterator<UploadAction> iterator() {
        return images.iterator();
    }

    @Override
    public int size() {
        return images.size();
    }

    @Override
    public boolean offer(UploadAction uploadAction) {
        if (uploadAction == null)
            return (false);
        images.add(uploadAction);
        return (true);
    }

    @Override
    public UploadAction poll() {
        Iterator<UploadAction> iterator;
        UploadAction uploadAction;

        if (size() <= 0)
            return (null);
        iterator = iterator();
        uploadAction = iterator.next();
        if(uploadAction != null){
            iterator.remove();
            return (uploadAction);
        }
        return (null);
    }

    @Override
    public UploadAction peek() {
        return images.getFirst();
    }

}
