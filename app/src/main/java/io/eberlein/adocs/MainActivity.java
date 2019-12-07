package io.eberlein.adocs;

import android.os.Bundle;
import android.util.Log;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.google.android.material.navigation.NavigationView;
import com.liulishuo.filedownloader.FileDownloader;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private static final String LOG_TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;

    private void createBaseFolder(){
        String sd = SDCardUtils.getSDCardPathByEnvironment();
        Log.d(LOG_TAG, sd);
        if(!FileUtils.createOrExistsDir(sd + "/adocs/")){
            Log.e(LOG_TAG, "could not create base folder");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDownloader.setup(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                Log.d(LOG_TAG, "permission storage granted");
                createBaseFolder();
            }

            @Override
            public void onDenied() {
                Log.d(LOG_TAG, "storage permission denied");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
