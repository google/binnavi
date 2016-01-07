/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.Database.cache;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeCache {

  private class NodeIdentifier {
    private IAddress address;
    private Integer moduleId;

    public NodeIdentifier(final IAddress address, final Integer moduleId) {
      this.address = address;
      this.moduleId = moduleId;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.address, this.moduleId);
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (other instanceof NodeIdentifier) {
        return Objects.equals(address, ((NodeIdentifier) other).address)
            && Objects.equals(moduleId, ((NodeIdentifier) other).moduleId);
      }
      return false;
    }
  }

  private static Map<SQLProvider, NodeCache> caches = new HashMap<SQLProvider, NodeCache>();

  Cache<Integer, INaviViewNode> nodeByIdCache = CacheBuilder.newBuilder().weakValues().build();
  Cache<NodeIdentifier, List<Integer>> addressModuleIdNodeIdsCache =
      CacheBuilder.newBuilder().build();

  private final SQLProvider provider;

  private final SQLProviderListener providerListener = new InternalSQLProviderListener();

  private NodeCache(final SQLProvider provider) {
    this.provider = provider;
    this.provider.addListener(providerListener);
  }

  public static synchronized NodeCache get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE01239: Provider argument can not be null");
    if (!caches.containsKey(provider)) {
      caches.put(provider, new NodeCache(provider));
    }
    return caches.get(provider);
  }

  private void close() {
    caches.remove(provider);
    provider.removeListener(providerListener);
  }

  public ImmutableCollection<INaviViewNode> getNodeByAddress(final IAddress nodeAddress,
      final Integer moduleId) {
    final List<Integer> nodeIds =
        addressModuleIdNodeIdsCache.getIfPresent(new NodeIdentifier(nodeAddress, moduleId));
    return nodeByIdCache.getAllPresent(nodeIds).values();
  }

  public void addNodes(final List<INaviViewNode> nodes) {
    nodeByIdCache.putAll(Maps.uniqueIndex(nodes, new Function<INaviViewNode, Integer>() {
      @Override
      public Integer apply(final INaviViewNode node) {
        return node.getId();
      }
    }));

    for (final INaviViewNode node : nodes) {
      if (node instanceof INaviCodeNode) {
        final IAddress nodeAddress = ((INaviCodeNode) node).getAddress();
        Integer moduleId = null;
        try {
          moduleId =
              ((INaviCodeNode) node).getParentFunction().getModule().getConfiguration().getId();
        } catch (final MaybeNullException e) {
          continue;
        }
        if (moduleId != null) {
          UpdateAddressModuleIdCache(nodeAddress, moduleId, node);
        }
      }
    }
  }

  private void UpdateAddressModuleIdCache(IAddress nodeAddress, Integer moduleId,
      INaviViewNode node) {
    final NodeIdentifier nodeAddressModuleId = new NodeIdentifier(nodeAddress, moduleId);
    List<Integer> nodeIds = addressModuleIdNodeIdsCache.getIfPresent(nodeAddressModuleId);
    if (nodeIds != null) {
      nodeIds.add(node.getId());
    } else {
      nodeIds = Lists.newArrayList(node.getId());
    }
    addressModuleIdNodeIdsCache.put(nodeAddressModuleId, nodeIds);
  }

  public INaviViewNode getNodeById(final Integer nodeId) {
    return nodeByIdCache.getIfPresent(nodeId);
  }

  /**
   * Internal listener class to keep informed about changes in the {@link SQLProvider provider}.
   */
  private class InternalSQLProviderListener implements SQLProviderListener {

    @Override
    public void providerClosing(SQLProvider provider) {
      if (NodeCache.this.provider.equals(provider)) {
        NodeCache.this.close();
      }
    }
  }
}
