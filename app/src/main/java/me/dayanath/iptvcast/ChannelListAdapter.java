package me.dayanath.iptvcast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import me.dayanath.iptvcast.m3u.ChannelItem;
import me.dayanath.iptvcast.m3u.ChannelList;

public class ChannelListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ChannelList mDataSource;
    private Context mContext;

    public ChannelListAdapter(@NonNull Context context, ChannelList cl) {
        mContext = context;
        mDataSource = cl;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.items.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = mInflater.inflate(R.layout.channel_list_row, parent, false);
        ChannelItem c = (ChannelItem) getItem(position);

        TextView name = (TextView) rowView.findViewById(R.id.channelName);

        name.setText(c.name);
        return rowView;
    }
}
