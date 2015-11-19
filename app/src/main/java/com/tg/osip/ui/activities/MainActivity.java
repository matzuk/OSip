package com.tg.osip.ui.activities;

import android.content.Context;
import android.os.Bundle;
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

import com.tg.osip.R;
import com.tg.osip.business.main.MainController;
import com.tg.osip.ui.chats.ChatsFragment;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private View headerNavigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private MainController mainController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        initNavigationHeaderView();
        setToolbar();
        initView();
        if (mainController == null) {
            mainController = new MainController();
        }
        mainController.setHeaderNavigationView(headerNavigationView);
        if (savedInstanceState == null) {
            startFragment(new ChatsFragment(), false);
        }
    }

    private void initNavigationHeaderView() {
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        headerNavigationView = layoutInflater.inflate(R.layout.navigation_header_layout, null);
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
                        case R.id.navigation_item_1:
                            drawerLayout.closeDrawers();
                            return true;
                        default:
                            return true;
                    }
                });
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
    public void onDestroy() {
        if (mainController != null) {
            mainController.onDestroy();
        }
        super.onDestroy();
    }

}
