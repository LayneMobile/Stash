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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import stash.internal.subscribers.LatchSubscriber;
import stash.internal.subscribers.ReferenceSubscriber;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StashTest {

    private final String testValue = "Stash Test";
    private final StashKey<String> testKey = new StashKey<String>() {
        @Override public String getKey() {
            return testValue;
        }
    };
    private final Stash<String> testStash
            = Stash.create(new WaitWorker(testKey, 3, TimeUnit.SECONDS));
    private final Observable<Entry<String>> getObservable = testStash.get().asObservable();
    private final Observable<String> getStringObservable = testStash.getData().asObservable();

    @Before
    public void setup() {
    }

    @After
    public void teardown() {
    }

    @Test
    public void shouldReturnTestEntryAsCallable() throws Exception {
        Entry<String> entry = StashObservables.callable(getObservable)
                .call();

        Assert.assertNotNull(entry);
        Assert.assertNotNull(entry.getMetadata());
        Assert.assertEquals(0, entry.getMetadata().getData().size());
        Assert.assertEquals(testValue, entry.getData());
    }

    @Test
    public void shouldReturnTestValueAsCallable() throws Exception {
        String returnValue = StashObservables.callable(
                getStringObservable.subscribeOn(Schedulers.io()))
                .call();
        Assert.assertEquals(testValue, returnValue);
    }

    @Test
    public void shouldBlockNormally() {
        ReferenceSubscriber<String> subscriber = new ReferenceSubscriber<String>();
        getStringObservable.subscribe(subscriber);
        Assert.assertNull(subscriber.getError());
        Assert.assertTrue(subscriber.isCompleted());
        Assert.assertEquals(testValue, subscriber.getLatest());
    }

    @Test
    public void shouldReceiveResultFromAsyncScheduler() throws Exception {
        LatchSubscriber<String> subscriber = new LatchSubscriber<String>();
        getStringObservable.subscribeOn(Schedulers.io())
                .subscribe(subscriber);
        subscriber.await(10, TimeUnit.SECONDS);
        Assert.assertNull(subscriber.getError());
        Assert.assertTrue(subscriber.isCompleted());
        Assert.assertEquals(testValue, subscriber.getLatest());
    }

    @Test
    public void shouldNotBlockWhenSubscribingOnScheduler() {
        final AtomicBoolean called = new AtomicBoolean(false);
        getStringObservable.subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override public void call(String s) {
                        called.set(true);
                    }
                });
        Assert.assertFalse(called.get());
    }

    @Test
    public void shouldBeAbleToUnsubscribeFromStashObservable() throws Exception {
        LatchSubscriber<String> subscriber = new LatchSubscriber<String>();
        Subscription sub = getStringObservable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(subscriber);
        sub.unsubscribe();
        subscriber.await(10, TimeUnit.SECONDS);
        Assert.assertNull(subscriber.getError());
        Assert.assertFalse(subscriber.isCompleted());
        Assert.assertNull(subscriber.getLatest());
    }

    private static final class WaitWorker implements Stash.Worker<String> {
        private final StashKey<String> key;
        private final long waitMillis;

        private WaitWorker(StashKey<String> key, long time, TimeUnit unit) {
            this.key = key;
            this.waitMillis = unit.toMillis(time);
        }

        @Override public Entry<String> get() throws Exception {
            Thread.sleep(waitMillis);
            return new Entry.Builder<String>()
                    .setData(key.getKey())
                    .build();
        }

        @Override public String put(@NonNull Entry<String> entry) throws Exception {
            Thread.sleep(waitMillis);
            return entry.getData();
        }

        @Override public boolean remove() throws Exception {
            Thread.sleep(waitMillis);
            return true;
        }
    }
}
