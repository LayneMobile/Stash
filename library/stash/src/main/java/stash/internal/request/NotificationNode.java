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

package stash.internal.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Notification;

final class NotificationNode<T> {
    final NotificationNode<T> prev;
    final Notification<T> current;

    NotificationNode() {
        this(null, null);
    }

    NotificationNode(NotificationNode<T> prev, Notification<T> current) {
        this.prev = prev;
        this.current = current;
    }

    NotificationNode<T> update(Notification<T> current) {
        return new NotificationNode<T>(this, current);
    }

    @NonNull List<Notification<T>> prevList() {
        return prevList(null);
    }

    @NonNull List<Notification<T>> prevList(@Nullable NotificationNode<T> until) {
        if (this == until) {
            return Collections.emptyList();
        }

        NotificationNode<T> prev = this.prev;
        List<Notification<T>> list = null;
        while (prev != null
                && prev.current != null
                && (until == null || prev != until)) {
            if (list == null) {
                list = new ArrayList<Notification<T>>();
            }
            list.add(prev.current);
            prev = prev.prev;
        }
        if (list != null) {
            Collections.reverse(list);
            return list;
        }
        return Collections.emptyList();
    }

    @Override public String toString() {
        StringBuilder string = new StringBuilder("NotificationNode{")
                .append("current=")
                .append(current);
        NotificationNode<T> prev = this.prev;
        while (prev != null) {
            string.append(",\n\n   prev=NotificationNode{")
                    .append("current=")
                    .append(prev.current)
                    .append("}");
            prev = prev.prev;
        }
        return string.append("}").toString();
    }
}
