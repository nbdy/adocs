package io.eberlein.adocs;

import com.blankj.utilcode.util.SPUtils;

import static android.content.Context.MODE_PRIVATE;

public class Static {
    public static final String BASE_DIRECTORY = "/adocs/";
    public static final String KEY_SP = "ADocs";
    public static final String KEY_SP_FAV = "favourites";

    public static SPUtils getFavouriteSP(){
        return SPUtils.getInstance(Static.KEY_SP + Static.KEY_SP_FAV, MODE_PRIVATE);
    }
}
