package com.tg.osip.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tg.osip.R;
import com.tg.osip.business.main.MainInteract;
import com.tg.osip.business.models.UserItem;
import com.tg.osip.ui.chats.ChatsFragment;
import com.tg.osip.ui.general.DefaultSubscriber;
import com.tg.osip.ui.general.views.images.PhotoView;
import com.tg.osip.utils.common.BackgroundExecutor;

import org.drinkless.td.libcore.telegram.TdApi;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private View headerNavigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Dialog logoutProgressDialog;
    private MainInteract mainInteract = new MainInteract();
    private Subscription getMeUserSubscription;
    private Subscription logoutSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        initNavigationHeaderView();
        setToolbar();
        initView();
        loadDataForNavigationHeaderView();
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
                        case R.id.navigation_item_logout:
                            drawerLayout.closeDrawers();
                            logout();
                            return true;
                        default:
                            return true;
                    }
                });
    }

    private void logout() {
        logoutSubscription = mainInteract.getLogoutObservable(() -> {
            logoutProgressDialog = createProgressDialog(MainActivity.this);
            logoutProgressDialog.show();
        }, () -> {
            if (logoutProgressDialog != null && logoutProgressDialog.isShowing()) {
                logoutProgressDialog.dismiss();
            }
        })
        .subscribe(new DefaultSubscriber<TdApi.AuthState>() {
            @Override
            public void onNext(TdApi.AuthState authState) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
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

    private void loadDataForNavigationHeaderView() {
        getMeUserSubscription = mainInteract.getMeUserObservable()
                .subscribeOn(Schedulers.from(BackgroundExecutor.getSafeBackgroundExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindView);
    }

    private void bindView(UserItem userItem) {
        if (userItem == null || headerNavigationView == null) {
            return;
        }
        PhotoView avatar = (PhotoView) headerNavigationView.findViewById(R.id.avatar);
        avatar.setCircleRounds(true);
        avatar.setImageLoaderI(userItem);
        TextView name = (TextView) headerNavigationView.findViewById(R.id.name);
        name.setText(userItem.getName());
        TextView phone = (TextView) headerNavigationView.findViewById(R.id.phone);
        phone.setText(userItem.getPhone());
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
        if (getMeUserSubscription != null && !getMeUserSubscription.isUnsubscribed()) {
            getMeUserSubscription.unsubscribe();
        }
        if (logoutSubscription != null && !logoutSubscription.isUnsubscribed()) {
            logoutSubscription.unsubscribe();
        }
        if (logoutProgressDialog != null && logoutProgressDialog.isShowing()) {
            logoutProgressDialog.dismiss();
        }
        super.onDestroy();
    }

}
