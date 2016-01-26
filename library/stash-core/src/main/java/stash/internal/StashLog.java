/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stash.internal;

import java.util.Map;

import stash.util.Logger;


public final class StashLog {
    private static volatile Logger sLogger = Logger.NONE;

    private StashLog() {}

    public static void setLogger(Logger logger) {
        sLogger = (logger == null)
                ? Logger.NONE
                : logger;
    }

    public static void v(final String tag, final String msg) {
        sLogger.v(tag, msg);
    }

    public static void v(final String tag, final String format, final Object... args) {
        sLogger.v(tag, String.format(format, args));
    }

    public static void v(final String tag, final String msg, final Throwable tr) {
        sLogger.v(tag, msg, tr);
    }

    public static void d(final String tag, final String msg) {
        sLogger.d(tag, msg);
    }

    public static void d(final String tag, final String format, final Object... args) {
        sLogger.d(tag, String.format(format, args));
    }

    public static void d(final String tag, final String msg, final Throwable tr) {
        sLogger.d(tag, msg, tr);
    }

    public static void i(final String tag, final String msg) {
        sLogger.i(tag, msg);
    }

    public static void i(final String tag, final String format, final Object... args) {
        sLogger.i(tag, String.format(format, args));
    }

    public static void i(final String tag, final String msg, final Throwable tr) {
        sLogger.i(tag, msg, tr);
    }

    public static void w(final String tag, final String msg) {
        sLogger.w(tag, msg);
    }

    public static void w(final String tag, final String format, final Object... args) {
        sLogger.w(tag, String.format(format, args));
    }

    public static void w(final String tag, final String msg, final Throwable tr) {
        sLogger.w(tag, msg, tr);
    }

    public static void e(final String tag, final String msg) {
        sLogger.e(tag, msg);
    }

    public static void e(final String tag, final String format, final Object... args) {
        sLogger.e(tag, String.format(format, args));
    }

    public static void e(final String tag, final String msg, final Throwable tr) {
        sLogger.e(tag, msg, tr);
    }

    public static void printAllStackTraces(final String tag) {
        for (Map.Entry<Thread, StackTraceElement[]> thread : Thread.getAllStackTraces().entrySet()) {
            StashLog.e(tag, "\nthread: %s", thread.getKey().getName());
            printStackTrace(tag, thread.getValue());
        }
    }

    private static void printStackTrace(String tag, StackTraceElement[] stackTrace) {
        for (StackTraceElement element : stackTrace) {
            StashLog.e(tag, "'    %s'", element);
        }
    }
}
