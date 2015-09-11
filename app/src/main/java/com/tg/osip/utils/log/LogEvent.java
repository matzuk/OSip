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

/**
 * @author =Troy=
 * @version 1.0
 */
public class LogEvent {

    private final String mAppName;
    private final Level mLevel;
    private final String mThreadName;
    private final StackTraceElement mCaller;
    private final Object[] mMessages;

    private LogEvent(Builder builder) {
        mAppName = builder.mAppName;
        mLevel = builder.mLevel;
        mThreadName = builder.mThreadName;
        mCaller = builder.mCaller;
        mMessages = builder.mMessages;
    }

    public String getAppName() {
        return mAppName;
    }

    public Level getLevel() {
        return mLevel;
    }

    public String getThreadName() {
        return mThreadName;
    }

    public StackTraceElement getCaller() {
        return mCaller;
    }

    public Object[] getMessages() {
        return mMessages;
    }

    public static enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL,
        WTF
    }

    public static class Builder {

        private String mAppName;
        private Level mLevel;
        private String mThreadName;
        private StackTraceElement mCaller;
        private Object[] mMessages;

        public Builder setAppName(String appName) {
            mAppName = appName;
            return this;
        }

        public Builder setLevel(Level level) {
            mLevel = level;
            return this;
        }

        public Builder setThreadName(String threadName) {
            mThreadName = threadName;
            return this;
        }

        public Builder setCaller(StackTraceElement caller) {
            mCaller = caller;
            return this;
        }

        public Builder setMessages(Object... messages) {
            mMessages = messages;
            return this;
        }

        public LogEvent build() {
            return new LogEvent(this);
        }

    }

}
