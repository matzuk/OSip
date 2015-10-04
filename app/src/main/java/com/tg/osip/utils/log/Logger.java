/*
 * Copyright (c) 2011-2013 =Troy= (dvp.troy@gmail.com)
 *
 * This file is part of DroidKit Library, https://github.com/dev-troy/droidkit
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tg.osip.utils.log;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tg.osip.utils.log.appender.LogAppender;
import com.tg.osip.utils.log.config.DefaultLoggerConfig;
import com.tg.osip.utils.log.config.LoggerConfig;
import com.tg.osip.utils.log.filter.LogFilter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

/**
 * Logger only for DEBUG mode because Logger will not work if apk was obfuscating
 *
 * @author =Troy=
 * @author e.matsyuk
 * @version 1.0
 */
public class Logger {

    private static final String META_LOG_LEVEL = "utils.log.LEVEL";

    private String mAppName;
    private LogEvent.Level mLogLevel;
    private LoggerConfig mConfig;

    public static void registerLogger(Context context) {
        configure(context, new DefaultLoggerConfig());
    }

    public static void configure(Context context, LoggerConfig config) {
        if (LoggerHolder.INSTANCE.mConfig == null) {
            final PackageManager pm = context.getPackageManager();
            try {
                final ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                LoggerHolder.INSTANCE.mAppName = ai.loadLabel(pm).toString();
                if (ai.metaData != null) {
                    final String logLevel = ai.metaData.getString(META_LOG_LEVEL);
                    if (!TextUtils.isEmpty(logLevel)) {
                        LoggerHolder.INSTANCE.mLogLevel = LogEvent.Level.valueOf(logLevel);
                        if (LoggerHolder.INSTANCE.mLogLevel != null) {
                            LoggerHolder.INSTANCE.mConfig = config;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void debug(Object... message) {
        LoggerHolder.INSTANCE.log(LogEvent.Level.DEBUG, message);
    }

    public static void info(Object... message) {
        LoggerHolder.INSTANCE.log(LogEvent.Level.INFO, message);
    }

    public static void warn(Object... message) {
        LoggerHolder.INSTANCE.log(LogEvent.Level.WARN, message);
    }

    public static void error(Object... message) {
        if (message == null) {
            LoggerHolder.INSTANCE.log(LogEvent.Level.ERROR, "Unknown error");
        }
        LoggerHolder.INSTANCE.log(LogEvent.Level.ERROR, message);
    }

    public static void error(Throwable tr) {
        LoggerHolder.INSTANCE.log(LogEvent.Level.ERROR, getStackTraceString(tr));
    }

    public static void fatal(Object... message) {
        LoggerHolder.INSTANCE.log(LogEvent.Level.FATAL, message);
    }

    public static void wtf(Object... message) {
        LoggerHolder.INSTANCE.log(LogEvent.Level.WTF, message);
    }

    private void log(LogEvent.Level level, Object... message) {
        if (mConfig != null) {
            if (level.ordinal() >= mLogLevel.ordinal()) {
                final Thread thread = Thread.currentThread();
                final LogEvent event = new LogEvent.Builder()
                        .setAppName(mAppName)
                        .setLevel(level)
                        .setThreadName(thread.getName())
                        .setCaller(findCaller(thread))
                        .setMessages(message)
                        .build();
                for (LogFilter f : mConfig.getLogFilters()) {
                    if (!f.filter(event)) return;
                }
                for (LogAppender a : mConfig.getLogAppenders()) {
                    a.append(event);
                }
            }
        }
    }

    private static StackTraceElement findCaller(Thread thread) {
        final String loggerClassName = Logger.class.getName();
        boolean lastCallerIsLoggerClass = false;
        for (StackTraceElement caller : thread.getStackTrace()) {
            final boolean isLoggerClass = caller.getClassName().equalsIgnoreCase(loggerClassName);
            if (lastCallerIsLoggerClass && !isLoggerClass) {
                return caller;
            }
            lastCallerIsLoggerClass = isLoggerClass;
        }
        return thread.getStackTrace()[4];
    }

    private static final class LoggerHolder {

        public static final Logger INSTANCE = new Logger();

    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

}
