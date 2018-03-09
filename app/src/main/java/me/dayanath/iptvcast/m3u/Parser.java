package me.dayanath.iptvcast.m3u;

import android.util.Log;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    static String CHANNEL_REGEX = "EXTINF:(.+),(.+)(?:\\R)(.+)$";
    static String METADATA_REGEX = "(\\S+?)=\"(.+?)\"";

    public static ChannelList parse(InputStream playlist) {
        ChannelList cl = new ChannelList();
        Scanner s = new Scanner(playlist).useDelimiter("#");
        s.next();
        if(!s.next().equals("EXTM3U")) {
            throw new IllegalArgumentException();
        }
        while(s.hasNext()) {
            String line = s.next();
            Pattern pattern = Pattern.compile(CHANNEL_REGEX, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(line);
            if(!matcher.find()) {
                throw new IllegalArgumentException();
            }
            ChannelItem item = new ChannelItem();
            parseMetadata(item, matcher.group(1));
            item.name = matcher.group(2);
            item.url = matcher.group(3);
            cl.add(item);
        }
        return cl;
    }

    private static void parseMetadata(ChannelItem item, String metadata) {
        Pattern pattern = Pattern.compile(METADATA_REGEX);
        Matcher matcher = pattern.matcher(metadata);

        while (matcher.find()) {
            item.metadata.put(matcher.group(1), matcher.group(2));
        }
    }

}
