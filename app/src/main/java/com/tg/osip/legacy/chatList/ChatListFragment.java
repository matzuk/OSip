package com.tg.osip.legacy.chatList;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tg.osip.R;
import com.tg.osip.tdclient.TGProxy;
import com.tg.osip.ui.chat.ChatFragment;
import com.tg.osip.utils.ui.RecyclerItemClickListener;
import com.tg.osip.utils.ui.SimpleAlertDialogFragment;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

/**
 * @author e.matsyuk
 */
public class ChatListFragment extends Fragment {

    private static final int LIMIT = 60;
    private static final int START_OFFSET = 0;

    private RecyclerView recyclerView;
    private ChatListRecyclerAdapter recyclerViewAdapter;
    private GridLayoutManager recyclerViewLayoutManager;

    private boolean stopUpdateScrolling;

//    private FirstChatLoadingListener firstChatLoadingListener;

    // Toolbar + Navigation Drawer
    private Toolbar toolbar;
    private String toolbarTitle;

    private RecyclerView navigationDrawerRecyclerView;
    private RecyclerView.LayoutManager navigationDrawerLayoutManager;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean firstInitialisation = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_list_fragment, container, false);
        init(rootView);
        initToolbar(rootView);
        return rootView;
    }

    private void init(View view) {
        toolbarTitle = getResources().getString(R.string.chat_list_toolbar_title);

        // FIXME each reorientation = new request! need saved state for adapter or recycle view
        recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerViewChat);
        recyclerViewLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerViewLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerViewAdapter = new ChatListRecyclerAdapter();
        recyclerViewAdapter.setFirstInit(firstInitialisation);
        recyclerViewAdapter.setHasStableIds(true);
        recyclerView.setAdapter(recyclerViewAdapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        goToConcreteChat(position);
                    }
                })
        );

        stopUpdateScrolling = false;
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int position = recyclerViewLayoutManager.findLastVisibleItemPosition();
                int updatePosition = recyclerViewAdapter.getItemCount() - 1 - (60 / 2);
                if (!stopUpdateScrolling && position >= updatePosition) {
                    int offset = recyclerViewAdapter.getItemCount();
                    stopUpdateScrolling = true;
                    TGProxy.getInstance().getClientInstance().send(new TdApi.GetChats(offset, LIMIT), getUpdateScrollChatsResultHandler);
                }
            }
        });

        TGProxy.getInstance().getClientInstance().send(new TdApi.GetChats(START_OFFSET, LIMIT), getChatsResultHandler);
    }

//    @Override
//    public void showTop() {
//        Handler handler = new Handler(Looper.getMainLooper());
//        super.showTop();
//        if (firstInitialisation) {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    initToolbar();
//                }
//            });
//        } else {
//            firstInitialisation = true;
//            recyclerViewAdapter.setFirstInit(true);
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recyclerViewAdapter.notifyDataSetChanged();
//                }
//            }, 500);
//        }
//    }

    private void initToolbar(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        toolbar.setTitle(toolbarTitle);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void goToConcreteChat(int position) {
        long chatId = recyclerViewAdapter.getChat(position).id;
        ChatFragment chatFragment = ChatFragment.newInstance(chatId);
        startFragment(chatFragment);
    }

    private void startFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    private Client.ResultHandler getChatsResultHandler = new Client.ResultHandler() {
        @Override
        public void onResult(TdApi.TLObject object) {
            if (object == null) {
                return;
            }
            if (object instanceof TdApi.Chats && getActivity() != null) {
                initRecyclerView((TdApi.Chats)object);
            }  else if (object instanceof TdApi.Error) {
                String message = "ERROR \ncode:" + ((TdApi.Error)object).code + "\ntext:" + ((TdApi.Error)object).text;
                DialogFragment newDialog = SimpleAlertDialogFragment.newInstance(
                        getResources().getString(R.string.app_name), message);
                newDialog.show(getActivity().getSupportFragmentManager(), DialogFragment.class.getName());
            }
        }
    };

    private Client.ResultHandler getUpdateScrollChatsResultHandler = new Client.ResultHandler() {
        @Override
        public void onResult(TdApi.TLObject object) {
            if (object == null) {
                return;
            }
            if (object instanceof TdApi.Chats && getActivity() != null) {
                TdApi.Chats chats = (TdApi.Chats)object;
                if (chats.chats.length > 0) {
                    recyclerViewAdapter.addAllChat(chats);
                    stopUpdateScrolling = false;
                }
            }
        }
    };

    private void initRecyclerView(final TdApi.Chats chats) {
        Handler handler = new Handler(Looper.getMainLooper());
        if (firstInitialisation) {
            recyclerViewAdapter.addAllChat(chats);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    recyclerViewAdapter.addFirstAllChat(chats);
//                    firstChatLoadingListener.done();
                }
            });
        }
    }

    private void setToolbarTitle(boolean isConnect) {
        if(isConnect) {
            toolbarTitle = getResources().getString(R.string.chat_list_toolbar_title);
        } else {
            toolbarTitle = getResources().getString(R.string.chat_list_toolbar_title_waiting_network);
        }
        if (toolbar != null) {
            toolbar.setTitle(toolbarTitle);
        }
    }



    private void sendUpdateConcreteChat(long chatId) {
        if (recyclerViewAdapter.isChatInList(chatId)) {
            TGProxy.getInstance().getClientInstance().send(new TdApi.GetChat(chatId), getUpdatedChatResultHandler);
        }
    }

    private void sendNewMessage(long chatId) {
        if (recyclerViewAdapter.isChatInList(chatId)) {
            TGProxy.getInstance().getClientInstance().send(new TdApi.GetChat(chatId), getUpdatedChatResultHandler);
        } else {
            TGProxy.getInstance().getClientInstance().send(new TdApi.GetChat(chatId), getUpdateNewMessageChatResultHandler);
        }
    }

    private Client.ResultHandler getUpdatedChatResultHandler = new Client.ResultHandler() {
        @Override
        public void onResult(TdApi.TLObject object) {
            if (object instanceof TdApi.Chat && getActivity() != null) {
                recyclerViewAdapter.changeChat((TdApi.Chat)object);
            }
        }
    };

    private Client.ResultHandler getUpdateNewMessageChatResultHandler = new Client.ResultHandler() {
        @Override
        public void onResult(TdApi.TLObject object) {
            if (object instanceof TdApi.Chat && getActivity() != null) {
                recyclerViewAdapter.addChat((TdApi.Chat) object);
            }
        }
    };

}