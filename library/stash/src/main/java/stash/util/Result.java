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

public class Result<T> {
    private T data;
    private boolean success;
    private Throwable throwable;

    public static <T> Result<T> success(T data) {
        return new Result<T>(true, data, null);
    }

    public static <T> Result<T> failure(Throwable throwable) {
        return new Result<T>(false, null, throwable);
    }

    public Result() {}

    public Result(boolean success, T data, Throwable throwable) {
        this.success = success;
        this.data = data;
        this.throwable = throwable;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Exception getException() {
        if (throwable instanceof Exception) {
            return (Exception) throwable;
        }
        return new Exception(throwable);
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Result<T> copy() {
        return new Result<T>(success, getData(), throwable);
    }

    @Override
    public String toString() {
        return "CallableResult{" +
                "success=" + success +
                ", data=" + getData() +
                ", throwable=" + throwable +
                '}';
    }
}
