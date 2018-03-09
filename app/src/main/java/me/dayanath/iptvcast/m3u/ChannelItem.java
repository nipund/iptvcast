package me.dayanath.iptvcast.m3u;

import java.util.HashMap;

public class ChannelItem {
    public int duration;
    public String name, url;
    public HashMap<String, String> metadata;

    public ChannelItem() {
        metadata = new HashMap<String, String>();
    }
}
