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

package stash.sources.builder;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stash.Params;
import stash.Source;
import stash.internal.StashLog;
import stash.internal.Util;

final class SourceHandler {
    private static final String TAG = SourceHandler.class.getSimpleName();

    private SourceHandler() { throw new AssertionError("no instances"); }

    @SuppressWarnings("unchecked")
    static <T, P extends Params> Source<T, P> source(Collection<SourceHandlerModule> extensions) {
        Set<Class<? extends Source>> classes = new HashSet<Class<? extends Source>>();
        Map<String, List<SourceMethodHandler>> handlers = new HashMap<String, List<SourceMethodHandler>>();
        for (SourceHandlerModule extension : extensions) {
            classes.add(extension.source);
            for (Map.Entry<String, List<SourceMethodHandler>> entry : extension.handlers.entrySet()) {
                final String name = entry.getKey();
                List<SourceMethodHandler> current = handlers.get(name);
                if (current == null) {
                    current = new ArrayList<SourceMethodHandler>();
                    handlers.put(name, current);
                }
                current.addAll(entry.getValue());
            }
        }
        ClassLoader cl = Source.class.getClassLoader();
        Class[] ca = classes.toArray(new Class[classes.size()]);
        return (Source<T, P>) Proxy.newProxyInstance(cl, ca, new InvokeHandler(handlers));
    }

    private static class InvokeHandler implements InvocationHandler {
        private final Map<String, List<SourceMethodHandler>> handlers;

        private InvokeHandler(Map<String, List<SourceMethodHandler>> handlers) {
            this.handlers = Collections.unmodifiableMap(handlers);
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodResult result = new MethodResult();
            List<SourceMethodHandler> handlers = this.handlers.get(method.getName());
            for (SourceMethodHandler handler : Util.nullSafe(handlers)) {
                if (handler.handle(proxy, method, args, result)) {
                    StashLog.d(TAG, "handling method: %s", method);
                    StashLog.d(TAG, "result: %s", result.get());
                    return result.get();
                }
            }
            StashLog.w(TAG, "could not find handler for method: %s", method);
            return null;
        }
    }
}
