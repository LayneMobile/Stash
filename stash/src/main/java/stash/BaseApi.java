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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import stash.internal.StashModuleImpl;
import stash.params.ContextParams;

public class BaseApi<T, P extends Params> {
    private final RequestProcessor<T, P> requestProcessor;

    protected BaseApi(@NonNull RequestProcessor<T, P> requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    protected final RequestProcessor<T, P> requestProcessor() {
        return requestProcessor;
    }

    @Nullable public static Context getContext(@NonNull Params params) {
        Context context = null;
        if (params instanceof ContextParams) {
            context = ((ContextParams) params).getContext();
        }
        if (context == null) {
            context = StashModuleImpl.getInstance().getContext();
        }
        return context;
    }

    // TODO: need to add Required validate to generated builders
//
//    public static abstract class AbstractBuilder<T, P extends Params, API extends BaseApi<T, P>>
//            implements Builder<T, P, API> {
//        private final SourceBuilder<T, P> builder = new SourceBuilder<T, P>();
//        private boolean superCalled;
//
//        @NonNull protected AbstractBuilder<T, P, API> source(@NonNull Action2<P, Subscriber<? super T>> action) {
//            return module(new Source.Module<T, P>()
//                    .source(action)
//                    .build());
//        }
//
//        @NonNull protected AbstractBuilder<T, P, API> source(@NonNull Func1<P, T> func) {
//            return module(new Source.Module<T, P>()
//                    .source(func)
//                    .build());
//        }
//
//        @NonNull protected AbstractBuilder<T, P, API> requiresNetwork() {
//            return module(new NetworkSource.Module()
//                    .build());
//        }
//
//        @NonNull protected AbstractBuilder<T, P, API> requiresNetwork(@NonNull NetworkChecker networkChecker) {
//            return module(new NetworkSource.Module()
//                    .requiresNetwork(networkChecker)
//                    .build());
//        }
//
//        @NonNull protected AbstractBuilder<T, P, API> aggregate(@NonNull Func1<P, Aggregable> func) {
//            return module(new AggregableSource.Module<P>()
//                    .aggregate(func)
//                    .build());
//        }
//
//        @NonNull public AbstractBuilder<T, P, API> module(@NonNull SourceModule module) {
//            builder.module(module);
//            return this;
//        }
//
//        @NonNull public AbstractBuilder<T, P, API> modules(@NonNull SourceModule... modules) {
//            builder.modules(modules);
//            return this;
//        }
//
//        @NonNull public AbstractBuilder<T, P, API> modules(@NonNull List<SourceModule> modules) {
//            builder.modules(modules);
//            return this;
//        }
//
//        protected final Source<T, P> source() {
//            final Required required = new Required();
//            superCalled = false;
//            requires(required);
//            if (!superCalled) {
//                throw new IllegalStateException("requires() method must call super");
//            }
//            builder.verifyContains(required.required);
//            return builder.build();
//        }
//
//        protected final RequestProcessor<T, P> requestProcessor() {
//            return new RequestProcessor.Builder<T, P>()
//                    .setSource(source())
//                    .build();
//        }
//
//        protected void requires(@NonNull Required required) {
//            required.add(Source.class);
//            superCalled = true;
//        }
//
//        public <A extends API> Builder<T, P, A> extend(@NonNull Extender<T, P, A> extender) {
//            RequestProcessor<T, P> processor = new RequestProcessor.Builder<T, P>()
//                    .setSource(source())
//                    .build();
//            return extender.extend(processor);
//        }
//
//        protected static final class Required {
//            private final Set<Class<? extends Source>> required = new HashSet<Class<? extends Source>>();
//
//            private Required() {}
//
//            public Required add(Class<? extends Source> source) {
//                required.add(source);
//                return this;
//            }
//
//            public Required addAll(Class<? extends Source>... sources) {
//                return addAll(Util.list(sources));
//            }
//
//            public Required addAll(Collection<Class<? extends Source>> sources) {
//                required.addAll(sources);
//                return this;
//            }
//        }
//    }
}
