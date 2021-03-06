/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric8.kubernetes.client.dsl;

import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.builder.Visitable;
import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.client.FromServerGettable;
import io.fabric8.kubernetes.client.Watcher;

public interface VisitFromServerGetWatchDeleteRecreateWaitApplicable<T> extends Visitable<VisitFromServerGetWatchDeleteRecreateWaitApplicable<T>>,
  FromServerGettable<T>,
  Watchable<Watcher<T>>,
  Waitable<T, T>,
  VisitFromServerWritable<T>,
  DryRunable<VisitFromServerWritable<T>>,
  Readiable,
  Editable<T> {
  
  /**
   * @deprecated use {@link Editable#edit(Visitor...)} instead
   */
  @Override
  @Deprecated
  VisitFromServerGetWatchDeleteRecreateWaitApplicable<T> accept(Visitor... arg0);
  
  /**
   * @deprecated use {@link Editable#edit(Class, Visitor)} instead
   */
  @Override
  @Deprecated
  default <V> VisitFromServerGetWatchDeleteRecreateWaitApplicable<T> accept(Class<V> type, Visitor<V> visitor) {
    return accept(new TypedVisitor<V>() {@Override public Class<V> getType() {return type;} @Override public void visit(V element) {visitor.visit(element);}});
  }
}
