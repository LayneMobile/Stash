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


import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import stash.util.Result;
import stash.util.ResultCallable;

public final class StashObservables {
    private StashObservables() { throw new AssertionError("no instances"); }

    public static <T> Observable<T> from(final Func0<T> func) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override public void call(Subscriber<? super T> subscriber) {
                try {
                    T t = func.call();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(t);
                        subscriber.onCompleted();
                    }
                } catch (Throwable e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static <T> Observable<T> from(final Callable<T> callable) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override public void call(Subscriber<? super T> subscriber) {
                try {
                    T t = callable.call();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(t);
                        subscriber.onCompleted();
                    }
                } catch (Throwable e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static <T> Observable<T> from(final ResultCallable<T> callable) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override public void call(Subscriber<? super T> subscriber) {
                Result<T> result = callable.call();
                if (result.isSuccess()) {
                    subscriber.onNext(result.getData());
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(result.getException());
                }
            }
        });
    }

    public static <T> ResultCallable<T> resultCallable(@NonNull final Observable<T> observable) {
        return new ResultCallable<T>() {
            @NonNull @Override public Result<T> call() {
                try {
                    T data = callable(observable).call();
                    return Result.success(data);
                } catch (Throwable e) {
                    return Result.failure(e);
                }
            }
        };
    }

    public static <T> Callable<T> callable(@NonNull final Observable<T> observable) {
        return new Callable<T>() {
            @Override public T call() throws Exception {
                return observable.toBlocking().last();
            }
        };
    }
}
