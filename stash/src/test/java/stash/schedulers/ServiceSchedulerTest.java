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

package stash.schedulers;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ServiceController;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ServiceSchedulerTest {

    @Before
    public void setup() {
    }

    @After
    public void teardown() {
        TestService.sListener = null;
    }

    @Test
    public void shouldBeAbleToRunTestService() {
        ServiceController<TestService> serviceController = Robolectric.buildService(
                TestService.class);

        // assert not started
        TestService service = serviceController.attach().get();
        Assert.assertFalse(service.isRunning());

        // assert create starts the service
        service = serviceController.create().get();
        Assert.assertTrue(service.isRunning());

        // assert destroy stops the service
        service = serviceController.destroy().get();
        Assert.assertFalse(service.isRunning());
    }

    @Test
    public void shouldStartAndStopServiceWhenScheduled() throws InterruptedException {
        final Action0 action = mock(Action0.class);
        final Scheduler immediate = mock(Scheduler.class);
        final Scheduler.Worker immediateWorker = spy(new Scheduler.Worker() {
            final Scheduler.Worker actual = Schedulers.immediate().createWorker();

            @Override public Subscription schedule(Action0 action) {
                return actual.schedule(action);
            }

            @Override public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
                return actual.schedule(action, delayTime, unit);
            }

            @Override public void unsubscribe() {
                actual.unsubscribe();
            }

            @Override public boolean isUnsubscribed() {
                return actual.isUnsubscribed();
            }
        });
        // create mock immediate worker
        when(immediate.createWorker()).thenReturn(immediateWorker);

        final Context context = spy(Robolectric.setupActivity(Activity.class));
        // return mock when ServiceScheduler stores application context
        when(context.getApplicationContext()).thenReturn(context);

        ServiceScheduler scheduler = ServiceScheduler.create(context, TestService.class, immediate,
                0L,
                TimeUnit.SECONDS);
        Scheduler.Worker inner = scheduler.createWorker();
        inner.schedule(action);

        // verify that the context was called by the service to start and stop
        ArgumentCaptor<Intent> intent = ArgumentCaptor.forClass(Intent.class);
        verify(context).startService(intent.capture());

        // verify that the intent was TestService.class
        Assert.assertEquals(TestService.class.getName(),
                intent.getValue().getComponent().getClassName());

        // verify that real scheduler was called
        verify(immediate).createWorker();

        // verify that real worker was called
        ArgumentCaptor<Action0> actionCaptor = ArgumentCaptor.forClass(Action0.class);
        verify(immediateWorker).schedule(actionCaptor.capture());

        // Make call to source passed to scheduler and verify real source called
        Action0 serviceAction = actionCaptor.getValue();
        verify(action).call();
        // assert service source is unsubscribed
        Assert.assertTrue(((Subscription) serviceAction).isUnsubscribed());

        // Sleep a few millimicrots
        Thread.sleep(10);

        // Make sure service is stopped
        verify(context).stopService(intent.capture());

        // verify that the intent was TestService.class
        Assert.assertEquals(TestService.class.getName(),
                intent.getValue().getComponent().getClassName());
    }

    public static class TestService extends Service {
        static ServiceListener sListener;
        static boolean sRunning;
        boolean running;

        @Override
        public void onCreate() {
            super.onCreate();
            sRunning = running = true;
            if (sListener != null) {
                sListener.runningChanged(true);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            sRunning = running = false;
            if (sListener != null) {
                sListener.runningChanged(false);
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public boolean isRunning() {
            return running;
        }
    }

    public static interface ServiceListener {
        void runningChanged(boolean isRunning);
    }
}
