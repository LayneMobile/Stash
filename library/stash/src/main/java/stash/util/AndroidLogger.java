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

package stash.util;

import android.util.Log;

public interface AndroidLogger extends Logger {
    Logger BASIC = new AndroidLogger() {
        @Override public void v(String tag, String msg) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, msg);
            }
        }

        @Override public void v(String tag, String msg, Throwable tr) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, msg, tr);
            }
        }

        @Override public void d(String tag, String msg) {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, msg);
            }
        }

        @Override public void d(String tag, String msg, Throwable tr) {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, msg, tr);
            }
        }

        @Override public void i(String tag, String msg) {
            if (Log.isLoggable(tag, Log.INFO)) {
                Log.i(tag, msg);
            }
        }

        @Override public void i(String tag, String msg, Throwable tr) {
            if (Log.isLoggable(tag, Log.INFO)) {
                Log.i(tag, msg, tr);
            }
        }

        @Override public void w(String tag, String msg) {
            if (Log.isLoggable(tag, Log.WARN)) {
                Log.w(tag, msg);
            }
        }

        @Override public void w(String tag, String msg, Throwable tr) {
            if (Log.isLoggable(tag, Log.WARN)) {
                Log.w(tag, msg, tr);
            }
        }

        @Override public void e(String tag, String msg) {
            if (Log.isLoggable(tag, Log.ERROR)) {
                Log.e(tag, msg);
            }
        }

        @Override public void e(String tag, String msg, Throwable tr) {
            if (Log.isLoggable(tag, Log.ERROR)) {
                Log.e(tag, msg, tr);
            }
        }
    };

    Logger FULL = new AndroidLogger() {
        @Override public void v(String tag, String msg) {
            Log.v(tag, msg);
        }

        @Override public void v(String tag, String msg, Throwable tr) {
            Log.v(tag, msg, tr);
        }

        @Override public void d(String tag, String msg) {
            Log.d(tag, msg);
        }

        @Override public void d(String tag, String msg, Throwable tr) {
            Log.d(tag, msg, tr);
        }

        @Override public void i(String tag, String msg) {
            Log.i(tag, msg);
        }

        @Override public void i(String tag, String msg, Throwable tr) {
            Log.i(tag, msg, tr);
        }

        @Override public void w(String tag, String msg) {
            Log.w(tag, msg);
        }

        @Override public void w(String tag, String msg, Throwable tr) {
            Log.w(tag, msg, tr);
        }

        @Override public void e(String tag, String msg) {
            Log.e(tag, msg);
        }

        @Override public void e(String tag, String msg, Throwable tr) {
            Log.e(tag, msg, tr);
        }
    };
}
