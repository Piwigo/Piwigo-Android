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

package org.piwigo.helper;

import android.accounts.Account;

/**
 * Created by Jeff on 10/1/2017.
 *
 * https://stackoverflow.com/questions/1944656/android-global-variable
 */



public class CommonVars {

    private static CommonVars instance = new CommonVars();


    // Getter-Setters
    public static CommonVars getInstance() {
        return instance;
    }

    public static void setInstance(CommonVars instance) {
        CommonVars.instance = instance;
    }

    private int cat_selected;

    private Account account;



    private CommonVars() {

    }


    public int getValue() {
        return cat_selected ;
    }


    public void setValue(int catid) {
        this.cat_selected = catid;
    }

    public Account getAccount() {return account;}

    public void setAccount(Account acnt) {this.account = acnt;}


}
