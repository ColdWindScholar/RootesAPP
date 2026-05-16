package com.root.krscript.model;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hello on 2018/04/01.
*/
 
public abstract class ShellHandlerBase extends Handler {
    // 事件常量定义
    public static final int EVENT_START = 0;
    public static final int EVENT_READ = 2;
    public static final int EVENT_READ_ERROR = 4;
    public static final int EVENT_WRITE = 6;
    public static final int EVENT_EXIT = -2;

    // ANSI 转义码处理
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("(\u001B\\[[\\d;]*m)");
    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int[] ANSI_COLORS = {
            Color.BLACK,      // 30
            Color.RED,       // 31
            Color.GREEN,     // 32
            Color.YELLOW,    // 33
            Color.BLUE,       // 34
            Color.MAGENTA,   // 35
            Color.CYAN,       // 36
            Color.WHITE       // 37
    };

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case EVENT_EXIT:
                onExit(msg.obj);
                break;
            case EVENT_START:
                onStart(msg.obj);
                break;
            case EVENT_READ:
                onReader(msg.obj);
                break;
            case EVENT_READ_ERROR:
                onError(msg.obj);
                break;
            case EVENT_WRITE:
                onWrite(msg.obj);
                break;
        }
    }

    // 核心 ANSI 颜色解析方法
    private SpannableString parseAnsiColors(String text) {
        // 移除所有 ANSI 转义码得到纯净文本
        String cleanText = text.replaceAll("\u001B\\[[\\d;]*m", "");
        SpannableString spannable = new SpannableString(cleanText);

        Matcher matcher = ANSI_ESCAPE_PATTERN.matcher(text);
        Deque<Integer> colorStack = new ArrayDeque<>();
        colorStack.push(DEFAULT_COLOR);

        int lastEnd = 0;
        int offset = 0;

        while (matcher.find()) {
            int ansiStart = matcher.start();
            int ansiEnd = matcher.end();
            String escapeCode = matcher.group(1);

            // 计算在 cleanText 中的位置
            int cleanStart = ansiStart - offset;
            offset += (ansiEnd - ansiStart);

            // 处理颜色变化
            if (escapeCode.equals("\u001B[0m")) {
                if (colorStack.size() > 1) {
                    colorStack.pop();
                }
            } else {
                Integer color = parseEscapeCode(escapeCode);
                if (color != null) {
                    colorStack.push(color);
                }
            }

            // 应用当前颜色到后续文本
            if (cleanStart >= 0 && cleanStart <= spannable.length()) {
                spannable.setSpan(
                        new ForegroundColorSpan(colorStack.peek()),
                        cleanStart,
                        spannable.length(),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        return spannable;
    }

    private Integer parseEscapeCode(String escapeCode) {
        String codeStr = escapeCode.replaceAll("[^0-9;]", "");
        String[] codes = codeStr.split(";");
        for (String part : codes) {
            if (!part.isEmpty()) {
                int code = Integer.parseInt(part);
                if (code >= 30 && code <= 37) {
                    return ANSI_COLORS[code - 30];
                } else if (code == 39) {
                    return DEFAULT_COLOR;
                }
            }
        }
        return null;
    }

    // 事件处理方法
    protected void onReader(Object msg) {
        if (msg != null) {
            SpannableString coloredText = parseAnsiColors(msg.toString());
            updateLog(coloredText);
        }
    }

    protected void onError(Object msg) {
        if (msg != null) {
            SpannableString spannable = new SpannableString(msg.toString());
            spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    0,
                    spannable.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            updateLog(spannable);
        }
    }

    protected void onWrite(Object msg) {
        if (msg != null) {
            SpannableString spannable = new SpannableString(msg.toString());
            spannable.setSpan(
                    new ForegroundColorSpan(Color.GRAY),
                    0,
                    spannable.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            updateLog(spannable);
        }
    }

    // 抽象方法
    protected abstract void updateLog(SpannableString msg);
    protected abstract void onStart(Object msg);
    protected abstract void onExit(Object msg);
}