package io.eberlein.adocs;

import com.blankj.utilcode.util.SDCardUtils;

public class Static {
    public static final String TBL_DOCS = "docs";
    public static final String TBL_DEFAULT = "tbl_default";

    public static final String BASE_DIRECTORY = "/adocs/";

    public static String getSDAppDirectory(){
        return SDCardUtils.getSDCardPathByEnvironment() + BASE_DIRECTORY;
    }

    public static final int REQUEST_CODE_PERMISSION_STORAGE = 42;
    public static final int REQUEST_CODE_PERMISSION_NETWORK_STATE = 420;
    public static final int REQUEST_CODE_PERMISSION_INTERNET = 666;
}
