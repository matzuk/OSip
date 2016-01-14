package com.tg.osip.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tg.osip.ApplicationSIP;
import com.tg.osip.R;
import com.tg.osip.business.main.MainInteract;
import com.tg.osip.tdclient.TGProxyI;
import com.tg.osip.tdclient.update_managers.FileDownloaderManager;
import com.tg.osip.ui.chats.ChatsFragment;
import com.tg.osip.ui.main.MainContract;
import com.tg.osip.ui.main.MainPresenter;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

/**
 *
 */
public class MainActivity extends AppCompatActivity implements MainContract.View {

    @Inject
    MainPresenter mainPresenter;

    private Toolbar toolbar;
    private View headerNavigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Dialog logoutProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationSIP.get().applicationComponent().plus(new MainActivityModule()).inject(this);
        mainPresenter.bindView(this);

        setContentView(R.layout.ac_main);
        initNavigationHeaderView();
        setToolbar();
        initView();
        if (savedInstanceState == null) {
            startFragment(new ChatsFragment(), false);
        }
    }

    private void initNavigationHeaderView() {
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        headerNavigationView = layoutInflater.inflate(R.layout.navigation_header_layout, null);
        mainPresenter.loadDataForNavigationHeaderView(headerNavigationView);
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().show();
    }

    private void initView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_drawer);
        navigationView.addHeaderView(headerNavigationView);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_item_logout:
                            mainPresenter.logout(MainActivity.this, drawerLayout);
                            return true;
                        default:
                            return true;
                    }
                });
    }

    private Dialog createProgressDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .cancelable(false)
                .content(context.getResources().getString(R.string.wait))
                .progress(true, 0)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void startFragment(Fragment fragment, boolean withBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (withBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    // methods for fragments

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void drawerToggleSyncState() {
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    public View.OnClickListener getCommonNavigationOnClickListener() {
        return v -> {
            if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        };
    }

    @Override
    public void showProgress() {
        logoutProgressDialog = createProgressDialog(MainActivity.this);
        logoutProgressDialog.show();
    }

    @Override
    public void hideProgress() {
        if (logoutProgressDialog != null && logoutProgressDialog.isShowing()) {
            logoutProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        mainPresenter.onDestroy();
        if (logoutProgressDialog != null && logoutProgressDialog.isShowing()) {
            logoutProgressDialog.dismiss();
        }
        super.onDestroy();
    }


    @Subcomponent(modules = MainActivityModule.class)
    public interface MainActivityComponent {
        void inject(MainActivity mainActivity);
    }

    @Module
    public static class MainActivityModule {

        @Provides
        @NonNull
        public MainPresenter provideMainPresenter(@NonNull MainInteract mainInteract) {
            return new MainPresenter(mainInteract);
        }

        @Provides
        @NonNull
        public MainInteract provideMainInteract(@NonNull TGProxyI tgProxyI, @NonNull FileDownloaderManager fileDownloaderManager) {
            return new MainInteract(tgProxyI,fileDownloaderManager);
        }
    }

}
