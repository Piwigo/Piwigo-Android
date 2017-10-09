/*
 * Copyright 2017 Raphael Mack http://www.raphael-mack.de
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

package org.piwigo.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.piwigo.R;
import org.piwigo.ui.model.User;

import java.util.List;


public class AccountSelectionSpinnerAdapter extends ArrayAdapter<User> {
    private LayoutInflater flater;

    public AccountSelectionSpinnerAdapter(@NonNull Activity context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<User> objects) {
        super(context, resource, objects);
        flater = context.getLayoutInflater();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = getDropDownView(position, convertView, parent);
        /* so far we use exactly the same view for the closed spinner and in the dropdown but we change the colors */

        TextView username = (TextView) rowView.findViewById(R.id.aspin_username);
        TextView galleryUrlView = (TextView) rowView.findViewById(R.id.aspin_url);

        username.setTextColor(ContextCompat.getColor(getContext(), R.color.nav_selected_user));
        galleryUrlView.setTextColor(ContextCompat.getColor(getContext(), R.color.nav_selected_user));
        return rowView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        View rowView = convertView;

        if(rowView == null){
            rowView = flater.inflate(R.layout.account_selection_spinner_item, parent, false);
        }

        TextView username = (TextView) rowView.findViewById(R.id.aspin_username);
        username.setText(user.username);
        if(user.guest) {
            username.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        }else{
            username.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        TextView galleryUrlView = (TextView) rowView.findViewById(R.id.aspin_url);
        galleryUrlView.setText(user.url);

        ImageView iconView = (ImageView) rowView.findViewById(R.id.aspin_image);
/*      TODO: show gallery image
         iconView.setImageResource(rowItem.);
 */

        /* TODO: check if we could replace the account_selection_spinner_item by account_row */

        return rowView;
    }
}
