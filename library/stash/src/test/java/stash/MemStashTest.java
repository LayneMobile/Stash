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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import stash.internal.StashesImpl;
import stash.stashdbs.MemDb;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MemStashTest {
    private final MemDb memDb = StashesImpl.getInstance().memDb();
    private final StashCollection<Object, String> stringCollection = memDb.getStashCollection(String.class);

    @Before
    public void setup() throws Exception {
        stringCollection.removeAll().asCallable().call();
        memDb.registerMaxSize(String.class, 100);
    }

    @After
    public void teardown() {
    }

    @Test
    public void memStashShouldReturnStashedData() throws Exception {
        StringKey key1 = new StringKey("one");
        StringKey key2 = new StringKey("two");
        Stash<String> memStash1 = stringCollection.getStash(key1);
        Stash<String> memStash2 = stringCollection.getStash(key2);

        String val1 = "test1";
        String val2 = "test2";

        // assert memstash is empty
        String ret1 = memStash1.getData().asCallable().call();
        Assert.assertNull(ret1);
        String ret2 = memStash2.getData().asCallable().call();
        Assert.assertNull(ret2);

        boolean saved = memStash1.put(val1).asCallable().call() == val1;
        Assert.assertTrue(saved);
        saved = memStash2.put(val2).asCallable().call() == val2;
        Assert.assertTrue(saved);

        // assert memstash returns value
        ret1 = memStash1.getData().asCallable().call();
        Assert.assertEquals(val1, ret1);
        ret2 = memStash2.getData().asCallable().call();
        Assert.assertEquals(val2, ret2);
    }

    @Test
    public void memStashShouldRespectMaxSize() throws Exception {
        // set max size to one
        memDb.registerMaxSize(String.class, 1);

        StringKey key1 = new StringKey("one");
        StringKey key2 = new StringKey("two");
        Stash<String> memStash1 = stringCollection.getStash(key1);
        Stash<String> memStash2 = stringCollection.getStash(key2);

        String val1 = "test1";
        String val2 = "test2";

        // assert memstash is empty
        String ret1 = memStash1.getData().asCallable().call();
        Assert.assertNull(ret1);
        String ret2 = memStash2.getData().asCallable().call();
        Assert.assertNull(ret2);

        boolean saved = memStash1.put(val1).asCallable().call() == val1;
        Assert.assertTrue(saved);

        // assert memstash1 returns value
        ret1 = memStash1.getData().asCallable().call();
        Assert.assertEquals(val1, ret1);

        // save stash2 value
        saved = memStash2.put(val2).asCallable().call() == val2;
        Assert.assertTrue(saved);

        // Now assert memstash 1 returns null and memstash2 returns value
        ret1 = memStash1.getData().asCallable().call();
        Assert.assertNull(ret1);
        ret2 = memStash2.getData().asCallable().call();
        Assert.assertEquals(val2, ret2);
    }

    private static final class StringKey implements StashKey<String> {
        private final String key;

        public StringKey(String key) {
            this.key = key;
        }

        @Override public String getKey() {
            return key;
        }
    }
}
