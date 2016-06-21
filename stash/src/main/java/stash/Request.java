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

import rx.Notification;
import rx.Observable;
import rx.functions.Func1;
import rxsubscriptions.RxSubscriptions;
import rxsubscriptions.SubscriptionBuilder;
import stash.schedulers.StashSchedulers;
import stash.subscribers.StashSubscribers;
import stash.util.ResultCallable;

public class Request<T> {
    private final Observable<T> observable;

    protected Request(@NonNull Observable.OnSubscribe<T> onSubscribe) {
        this(Observable.create(onSubscribe));
    }

    protected Request(@NonNull Observable<T> observable) {
        this.observable = observable;
    }

    @NonNull public static <T> Request<T> just(@NonNull T t) {
        return new Request<T>(Observable.just(t));
    }

    @NonNull public static <T> Request<T> from(@NonNull Observable<T> observable) {
        return new Request<T>(observable);
    }

    @NonNull public static <T> Request<T> from(@NonNull Callable<T> callable) {
        return new Request<T>(StashObservables.from(callable));
    }

    @NonNull public static <T> Request<T> from(@NonNull ResultCallable<T> callable) {
        return new Request<T>(StashObservables.from(callable));
    }

    @NonNull public final Observable<T> asObservable() {
        return observable;
    }

    @NonNull public final Observable<T> asAsyncObservable() {
        return asObservable()
                .subscribeOn(StashSchedulers.service());
    }

    @NonNull public final Iterable<Notification<T>> asIterable() {
        return asObservable()
                .materialize()
                .toBlocking()
                .toIterable();
    }

    @NonNull public final Callable<T> asCallable() {
        return StashObservables.callable(asObservable());
    }

    @NonNull public final ResultCallable<T> asResultCallable() {
        return StashObservables.resultCallable(asObservable());
    }

    @NonNull public final <R> Request<R> map(@NonNull Func1<? super T, ? extends R> func) {
        return new Request<R>(observable.map(func));
    }

    @NonNull public final <R> Request<R> concatMap(@NonNull Func1<? super T, ? extends Observable<? extends R>> func) {
        return new Request<R>(observable.concatMap(func));
    }

    @NonNull public final SubscriptionBuilder<T> on(@NonNull RxSubscriptions subscriptions) {
        return subscriptions.with(asAsyncObservable());
    }

    @NonNull public final SubscriptionBuilder<T> onMain(@NonNull RxSubscriptions subscriptions) {
        return subscriptions.with(asAsyncObservable())
                .observeOnMainThread();
    }

    public static <T> Extender<T> extender() {
        return new Extender<T>() {
            @Override public Request<T> call(Observable.OnSubscribe<T> onSubscribe) {
                return new Request<T>(onSubscribe);
            }
        };
    }

    public final void execute() {
        asAsyncObservable()
                .subscribe(StashSubscribers.empty());
    }

    public interface Extender<T> extends Func1<Observable.OnSubscribe<T>, Request<T>> {}
}
