package com.xsonsui.maxball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.xsonsui.maxball.model.Lobby;
import com.xsonsui.maxball.nuts.model.NetAddress;

import java.util.List;

public class LobbyBrowserActivity extends Activity{
    LobbyManager lobbyManager = new LobbyManager();
    private String mSessionId;
    public LobbyManager.Listener<List<NetAddress>> listLobbiesListener = new LobbyManager.Listener<List<NetAddress>>() {
        @Override
        public void onSuccess(List<NetAddress> result) {
            mAdapter.clear();
            mAdapter.addAll(result);
            mAdapter.notifyDataSetInvalidated();
        }

        @Override
        public void onFailed() {
            Toast.makeText(LobbyBrowserActivity.this, "Something bad happened :(", Toast.LENGTH_SHORT).show();
        }
    };
    private LobbyManager.Listener<String> registerListener = new LobbyManager.Listener<String>() {


        @Override
        public void onSuccess(String sessionId) {
            mSessionId = sessionId;
            lobbyManager.listLobbies(listLobbiesListener);
        }

        @Override
        public void onFailed() {
            Toast.makeText(LobbyBrowserActivity.this, "Something bad happened :(", Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    private ListView listView;
    private ArrayAdapter<NetAddress> mAdapter;
    private ViewGroup layoutCreateLobby;
    private EditText editLobbyName;
    private LobbyManager.Listener<Lobby> createLobbyListener = new LobbyManager.Listener<Lobby>() {
        @Override
        public void onSuccess(Lobby lobby) {
            Intent i = new Intent(LobbyBrowserActivity.this, GameActivity.class);
            i.putExtra("lobby", lobby);
            startActivity(i);

        }

        @Override
        public void onFailed() {
            Toast.makeText(LobbyBrowserActivity.this, "Something bad happened :(", Toast.LENGTH_SHORT).show();
        }
    };
    private String playerName;
    private String playerAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Bundle extras = getIntent().getExtras();
        playerName = extras.getString("name");
        if(playerName.isEmpty())
            playerName = "guest" + (int)(Math.random()*100000);
        playerAvatar = playerName.substring(0, 1);

        layoutCreateLobby = (ViewGroup)findViewById(R.id.layoutCreateLobby);
        layoutCreateLobby.setVisibility(View.GONE);
        editLobbyName = (EditText)findViewById(R.id.editText);
        listView = (ListView)findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<NetAddress>(this, R.layout.lobby_list_item, R.id.textView);
        listView.setAdapter(mAdapter);
        findViewById(R.id.buttonRefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lobbyManager.listLobbies(listLobbiesListener);
            }
        });

        findViewById(R.id.buttonCreate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutCreateLobby.setVisibility(View.VISIBLE);

            }
        });
        layoutCreateLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutCreateLobby.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.buttonCreateLobby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editLobbyName.getText().toString();
                Intent i = new Intent(LobbyBrowserActivity.this, GameActivity.class);
                i.putExtra("action", "host");
                i.putExtra("lobbyName", name);
                i.putExtra("sessionId", mSessionId);
                i.putExtra("playerName", playerName);
                i.putExtra("playerAvatar", playerAvatar);
                startActivity(i);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                NetAddress netAddress = mAdapter.getItem(pos);
                Intent i = new Intent(LobbyBrowserActivity.this, GameActivity.class);
                i.putExtra("action", "join");
                i.putExtra("lobby", new Lobby(netAddress.srcAddress, netAddress.srcPort));
                i.putExtra("sessionId", mSessionId);
                i.putExtra("playerName", playerName);
                i.putExtra("playerAvatar", playerAvatar);
                startActivity(i);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        lobbyManager.registerPlayer(playerName, registerListener);
    }
}
