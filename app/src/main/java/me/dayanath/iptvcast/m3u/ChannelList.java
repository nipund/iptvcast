package me.dayanath.iptvcast.m3u;

import java.util.ArrayList;

public class ChannelList {
    public String name;
    public ArrayList<ChannelItem> items;

    public ChannelList() {
        items = new ArrayList<ChannelItem>();
    }

    public void add(ChannelItem item) {
        items.add(item);
    }
}
