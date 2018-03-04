package me.dayanath.iptvcast.m3u;

import android.graphics.drawable.Drawable;

class TvgLogo {
    public String url;
    public boolean cached;

    public TvgLogo(String url) {
        this.url = url;
        cached = false;
    }

    public Drawable load() {
        return null;
    }
}
