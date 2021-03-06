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
package io.fabric8.kubernetes.client;

public class V1EventingAPIGroupExtensionAdapter extends APIGroupExtensionAdapter<V1EventingAPIGroupClient> {
  @Override
  protected String getAPIGroupName() {
    return "events.k8s.io/v1";
  }

  @Override
  public Class<V1EventingAPIGroupClient> getExtensionType() {
    return V1EventingAPIGroupClient.class;
  }

  @Override
  protected V1EventingAPIGroupClient newInstance(Client client) {
    return new V1EventingAPIGroupClient(client);
  }
}
