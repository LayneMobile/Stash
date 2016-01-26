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

package stash;


public final class Progress<T> {
    private final Status status;
    private final T data;

    private Progress(Status status) {
        this(status, null);
    }

    private Progress(Status status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> Progress<T> pending() {
        return new Progress<T>(Status.PENDING);
    }

    public static <T> Progress<T> executing() {
        return new Progress<T>(Status.EXECUTING);
    }

    public static <T> Progress<T> fetchingFromStash() {
        return new Progress<T>(Status.FETCHING_FROM_STASH);
    }

    public static <T> Progress<T> receivedFromStash(T data) {
        return new Progress<T>(Status.RECEIVED_FROM_STASH, data);
    }

    public static <T> Progress<T> callingSource() {
        return new Progress<T>(Status.CALLING_SOURCE);
    }

    public static <T> Progress<T> receivedFromSource(T data) {
        return new Progress<T>(Status.RECEIVED_FROM_SOURCE, data);
    }

    public static <T> Progress<T> savingToStash() {
        return new Progress<T>(Status.SAVING_TO_STASH);
    }

    public static <T> Progress<T> savedToStash(T data) {
        return new Progress<T>(Status.SAVED_TO_STASH, data);
    }

    public static <T> Progress<T> complete() {
        return new Progress<T>(Status.COMPLETE);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    @Override public String toString() {
        return "Progress{" +
                "status=" + status +
                '}';
    }

    public enum Status {
        PENDING,
        EXECUTING,
        FETCHING_FROM_STASH,
        RECEIVED_FROM_STASH,
        CALLING_SOURCE,
        RECEIVED_FROM_SOURCE,
        SAVING_TO_STASH,
        SAVED_TO_STASH,
        COMPLETE
    }
}
