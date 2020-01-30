package io.eberlein.adocs;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.GsonUtils;
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
import io.eberlein.adocs.ui.FavouritesFragment;
import io.eberlein.adocs.ui.HomeFragment;
// import io.eberlein.adocs.ui.SearchFragment;
import io.eberlein.adocs.ui.ViewDocFragment;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private static final String TAG = "MainActivity";

    private List<String> lastViewed = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventNextUrl(EventNextUrl e){
        if(!lastViewed.contains(e.getUrl())) lastViewed.add(e.getUrl());
        invalidateOptionsMenu();
    }

    private void createBaseFolder(){
        String sd = SDCardUtils.getSDCardPathByEnvironment();
        Log.d(TAG, sd);
        if(!FileUtils.createOrExistsDir(sd + "/adocs/")){
            Log.e(TAG, "could not create base folder");
        }
    }

    private void replaceFragment(Fragment f){
        FragmentUtils.replace(getSupportFragmentManager(), f, R.id.nav_host_fragment, true);
    }

    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.nav_home:
                    replaceFragment(new HomeFragment()); break;
                case R.id.nav_fav:
                    replaceFragment(new FavouritesFragment()); break;
                // case R.id.nav_search:
                //     replaceFragment(new SearchFragment()); break;
            }
            drawer.closeDrawers();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDownloader.setup(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

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

    public boolean isFavourite(String url){
        return Static.getFavouriteSP().getBoolean(url, false);
    }

    public String getCurrentUrl(){
        return lastViewed.get(lastViewed.size() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_favourite){
            if(isFavourite(getCurrentUrl())) Static.getFavouriteSP().remove(getCurrentUrl());
            else Static.getFavouriteSP().put(getCurrentUrl(), true);
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(currentFragmentIsViewDoc()){
            getMenuInflater().inflate(R.menu.main, menu);
            for(int i = 0; i < menu.size(); i++){
                MenuItem mi = menu.getItem(i);
                if(mi.getItemId() == R.id.menu_favourite){
                    if(isFavourite(getCurrentUrl())) mi.setIcon(R.drawable.baseline_favorite_24);
                    else mi.setIcon(R.drawable.baseline_favorite_border_24);
                }
                menu.getItem(i).setVisible(true);
            }
        }
        return true;
    }

    private boolean currentFragmentIsViewDoc(){
        return getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment) instanceof ViewDocFragment;
    }

    private void popFragment(){
        FragmentUtils.pop(getSupportFragmentManager(), true);
    }

    @Override
    public void onBackPressed() {
        int c = FragmentUtils.getAllFragmentsInStack(getSupportFragmentManager()).size();
        Log.d(TAG, GsonUtils.toJson(lastViewed));
        if(c == 0){
            super.onBackPressed();
        } else {
            if(currentFragmentIsViewDoc()) {
                lastViewed.remove(getCurrentUrl());
                if (lastViewed.size() > 1) {
                    EventBus.getDefault().post(new EventViewUrl(getCurrentUrl()));
                } else {
                    popFragment();
                }
            } else {
                popFragment();
            }
        }
        invalidateOptionsMenu();
    }
}
