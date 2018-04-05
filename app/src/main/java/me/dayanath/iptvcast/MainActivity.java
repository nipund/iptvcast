package me.dayanath.iptvcast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Toast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import me.dayanath.iptvcast.m3u.ChannelItem;
import me.dayanath.iptvcast.m3u.ChannelList;
import me.dayanath.iptvcast.m3u.Parser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private CastSession mCastSession;
    private SessionManager mSessionManager;
    private final SessionManagerListener mSessionManagerListener =
            new SessionManagerListenerImpl();
    private Button button;
    public Handler mHandler;
    private ListView list;
    private EditText search;
    private Kryo kryo;
    private boolean alreadyCasting;
    private ChannelListAdapter cla;

    private class SessionManagerListenerImpl implements SessionManagerListener<CastSession> {
        @Override
        public void onSessionStarting(CastSession session) {

        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStartFailed(CastSession session, int i) {

        }

        @Override
        public void onSessionEnding(CastSession session) {

        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int i) {

        }

        @Override
        public void onSessionSuspended(CastSession session, int i) {

        }

        @Override
        public void onSessionEnded(CastSession session, int error) {
            if (session == mCastSession) {
                mCastSession = null;
            }
            invalidateOptionsMenu();
            finish();
        }

        @Override
        public void onSessionResuming(CastSession session, String s) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        kryo = new Kryo();
        mHandler = new Handler(Looper.getMainLooper());
        mSessionManager = CastContext.getSharedInstance(this).getSessionManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        list = (ListView) findViewById(R.id.channel_list);
        search = (EditText) findViewById(R.id.search);

        try {
            Input input = new Input(openFileInput("playlist.bin"));
            ChannelList cl = kryo.readObject(input, ChannelList.class);
            input.close();
            cla = new ChannelListAdapter(this, cl);
            list.setAdapter(cla);
        } catch (FileNotFoundException e) {
            Log.d("kryo", "Channel list cache not found.");
        } catch (Exception e) {}

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChannelItem ci = (ChannelItem) list.getAdapter().getItem(position);
                Util.castHLS(mHandler, mCastSession.getRemoteMediaClient(), ci.url);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cla.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        mCastSession = mSessionManager.getCurrentCastSession();
        mSessionManager.addSessionManagerListener(mSessionManagerListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSessionManager.removeSessionManagerListener(mSessionManagerListener);
        mCastSession = null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.import_local:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 42);
                return true;
            case R.id.import_remote:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Remote M3U");
                final EditText input = new EditText(this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OkHttpClient client = new OkHttpClient();
                        String url = input.getText().toString();
                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, final IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Error fetching URL - " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    final ChannelList cl = Parser.parse(response.body().byteStream());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            cla = new ChannelListAdapter(MainActivity.this, cl);
                                            list.setAdapter(cla);
                                            try {
                                                Output output = new Output(openFileOutput("playlist.bin", MODE_PRIVATE));
                                                kryo.writeObject(output, cl);
                                                output.close();
                                            } catch (FileNotFoundException e) {}
                                        }
                                    });
                                } catch (final IllegalArgumentException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Error parsing M3U",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    ChannelList cl = Parser.parse(inputStream);
                    cla = new ChannelListAdapter(this, cl);
                    list.setAdapter(cla);
                    Output output = new Output(openFileOutput("playlist.bin", MODE_PRIVATE));
                    kryo.writeObject(output, cl);
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
