/*
 * Copyright 2017 Phil Bayfield https://philio.me
 * Copyright 2017 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo.ui.main;;

/**
 * Created by Jeff on 9/26/2017.
 */

import android.arch.lifecycle.ViewModel;

import org.piwigo.io.model.ImageInfo;
import org.piwigo.io.model.ImageListResponse;
import org.piwigo.io.repository.ImageRepository;

import java.util.List;
import java.util.Observable;

import rx.Subscriber;
import rx.Subscription;

public class ImagesItemViewModel extends ViewModel {


    private final String url;
    private final String title;



    public ImagesItemViewModel(String url, String title) {
        this.url = url;
        this.title = title;


    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }


}
