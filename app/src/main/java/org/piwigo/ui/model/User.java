/*
 * Piwigo for Android
 *
 * Copyright (C) 2017 Raphael Mack http://www.raphael-mack.de
 * Copyright (C) 2016 Phil Bayfield https://philio.me
 * Copyright (C) 2016 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.model;

import android.accounts.Account;

/* instances of this class represent one "piwigo account", I.e. an url-username combination */
public class User {

    public boolean guest;

    public String url;

    public String username;

    /* the android account for that user */
    public Account account;

    /* TODO add image for the gallery (from the favicon?) */
}
