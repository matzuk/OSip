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

package com.tg.osip.utils.log.appender;

import android.text.TextUtils;

import com.tg.osip.utils.log.LogEvent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author =Troy=
 * @version 1.0
 */
public class LogCatAppender implements LogAppender {

    @Override
    public void append(LogEvent event) {
        final String tag = buildTag(event);
        final String message = buildMessage(event);

        switch (event.getLevel()) {
            case DEBUG:
                android.util.Log.d(tag, message);
                break;

            case INFO:
                android.util.Log.i(tag, message);
                break;

            case WARN:
                android.util.Log.w(tag, message);
                break;

            case ERROR:
            case FATAL:
                android.util.Log.e(tag, message);
                break;

            case WTF:
                android.util.Log.wtf(tag, message);
                break;
        }
    }

    protected String buildTag(LogEvent event) {
        final StackTraceElement caller = event.getCaller();
        final String cls = caller.getClassName();
        final String appName = event.getAppName();
        return (TextUtils.isEmpty(appName) ? "" : "[" + appName + "] ") +
                cls.substring(cls.lastIndexOf('.') + 1, cls.length()) + ':' +
                caller.getMethodName() + ':' + caller.getLineNumber();
    }

    protected String buildMessage(LogEvent event) {
        final Object[] messages = event.getMessages();
        if (messages != null && messages.length > 0) {
            if (messages.length == 1) {
                return buildSingleMessage(event, messages[0]);
            } else {
                StringBuilder sb = new StringBuilder("~");
                for (Object m : messages) {
                    sb.append("\n").append(buildSingleMessage(event, m));
                }
                return sb.toString();
            }
        }
        return "~";
    }

    protected String buildSingleMessage(LogEvent event, Object message) {
        if (message == null) return "null";
        if (message instanceof Throwable) {
            final Throwable e = (Throwable) message;
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            try {
                e.printStackTrace(pw);
            } finally {
                pw.flush();
                pw.close();
            }
            return "~\n" + e.getMessage() + "\n" + sw.toString();
        }
        return message.toString();
    }

}
