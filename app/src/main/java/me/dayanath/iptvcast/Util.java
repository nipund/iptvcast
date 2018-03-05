package me.dayanath.iptvcast;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import me.dayanath.iptvcast.m3u.ChannelItem;
import me.dayanath.iptvcast.m3u.ChannelList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Util {
    public static void castHLS(final Handler mHandler, final RemoteMediaClient rmc, String url) {
        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final MediaInfo mediaInfo = new MediaInfo.Builder(response.request().url().toString())
                        .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                        .setContentType("application/x-mpegurl")
                        .build();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        rmc.load(mediaInfo, true, 0);
                    }
                });
            }
        });
    }

    public static void fillTable(Activity activity, ChannelList cl) {
        TableLayout table = (TableLayout) activity.findViewById(R.id.channelTable);

        for(ChannelItem c : cl.items) {
            View tr = activity.getLayoutInflater().inflate(R.layout.table_row, null);
            TextView name = (TextView) tr.findViewById(R.id.channelName);
            ImageView logo = (ImageView) tr.findViewById(R.id.channelLogo);
            name.setText(c.name);
            table.addView(tr);
        }
    }
}
