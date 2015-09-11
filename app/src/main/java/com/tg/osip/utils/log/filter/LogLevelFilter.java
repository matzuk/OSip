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

package com.tg.osip.utils.log.filter;

import com.tg.osip.utils.log.LogEvent;

/**
 * @author =Troy=
 * @version 1.0
 */
public class LogLevelFilter implements LogFilter {

    @Override
    public boolean filter(LogEvent event) {
        try {
            final Class<?> callerClass = Class.forName(event.getCaller().getClassName());
            final LogLevel level = callerClass.getAnnotation(LogLevel.class);
            return level == null || level.value() == event.getLevel();
        } catch (ClassNotFoundException e) {
            // do nothing: never find ourselves here
        }
        return true;
    }

}
