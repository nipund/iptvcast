package me.dayanath.iptvcast.m3u;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    static String CHANNEL_REGEX = "#EXTINF:(.+),(.+)(?:\\R)(.+)$";

    public static ChannelList parse(String playlist) {
        ChannelList cl = new ChannelList();
        Scanner s = new Scanner(playlist).useDelimiter("#");
        if(!s.next().equals("EXTM3U")) {
            throw new IllegalArgumentException();
        }
        while(s.hasNext()) {
            String line = s.next();
            Pattern pattern = Pattern.compile(CHANNEL_REGEX, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(line);
            if(!matcher.matches()) {
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
    }

}
