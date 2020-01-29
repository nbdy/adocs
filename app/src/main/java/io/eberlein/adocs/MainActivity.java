package io.eberlein.adocs;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SDCardUtils;

import com.google.android.material.navigation.NavigationView;

import com.liulishuo.filedownloader.FileDownloader;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.adocs.objects.events.EventNextUrl;
import io.eberlein.adocs.objects.events.EventViewUrl;
import io.eberlein.adocs.ui.HomeFragment;
import io.eberlein.adocs.ui.ViewDocFragment;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private static final String TAG = "MainActivity";

    private AppBarConfiguration mAppBarConfiguration;

    private List<String> lastViewed = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventNextUrl(EventNextUrl e){
        lastViewed.add(e.getUrl());
    }

    private void createBaseFolder(){
        String sd = SDCardUtils.getSDCardPathByEnvironment();
        Log.d(TAG, sd);
        if(!FileUtils.createOrExistsDir(sd + "/adocs/")){
            Log.e(TAG, "could not create base folder");
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
                R.id.nav_home,
                R.id.nav_search,
                R.id.nav_fav
        ).setDrawerLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                createBaseFolder();
                FragmentUtils.replace(getSupportFragmentManager(), new HomeFragment(), R.id.nav_host_fragment);
            }

            @Override
            public void onDenied() {
                Toast.makeText(getApplicationContext(), "no storage permission given", Toast.LENGTH_LONG).show();
                finish();
            }
        }).request();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        int c = FragmentUtils.getAllFragmentsInStack(getSupportFragmentManager()).size();
        Log.d(TAG, String.valueOf(c));
        if(c == 0){
            super.onBackPressed();
        } else {
            if(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment) instanceof ViewDocFragment){
                if(lastViewed.size() > 1) {
                    EventBus.getDefault().post(new EventViewUrl(lastViewed.get(lastViewed.size() - 2)));
                    lastViewed.remove(lastViewed.size() - 1);
                }
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }
}
