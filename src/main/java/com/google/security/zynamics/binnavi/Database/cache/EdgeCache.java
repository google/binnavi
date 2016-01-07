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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.IAddressNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EdgeCache {

  private class EdgeIdentifier {
    private IAddress sourceAddress;
    private Integer sourceModuleId;
    private IAddress targetAddress;
    private Integer targetModuleId;

    public EdgeIdentifier(final IAddress sourceAddress, final Integer sourceModuleId,
        final IAddress targetAddress, final Integer targetModuleId) {
      this.sourceAddress = sourceAddress;
      this.sourceModuleId = sourceModuleId;
      this.targetAddress = targetAddress;
      this.targetModuleId = targetModuleId;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.sourceAddress, this.sourceModuleId, this.targetAddress,
          this.targetModuleId);
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (other instanceof EdgeIdentifier) {
        return Objects.equals(sourceAddress, ((EdgeIdentifier) other).sourceAddress)
            && Objects.equals(sourceModuleId, ((EdgeIdentifier) other).sourceModuleId)
            && Objects.equals(targetAddress, ((EdgeIdentifier) other).targetAddress)
            && Objects.equals(targetModuleId, ((EdgeIdentifier) other).targetModuleId);
      }
      return false;
    }
  }

  private static Map<SQLProvider, EdgeCache> caches = new HashMap<SQLProvider, EdgeCache>();

  /**
   * The edges by id cache stores weak values of the actual edges to evict them from the cache when
   * no further references exist which reference them.
   * https://code.google.com/p/guava-libraries/wiki/CachesExplained
   */
  Cache<Integer, INaviEdge> edgesByIdCache = CacheBuilder.newBuilder().weakValues().build();

  /**
   * The edge address to edge id cache is stores a list of the edge ids. This cache can in the
   * current design not be evicted as there is no way to have weak references work on the list of
   * node ids. Therefore we are leaking memory here. An alternative approach here would be to do the
   * evictions manually.
   */
  Cache<EdgeIdentifier, List<Integer>> edgeAddressToEdgeIdsCache =
      CacheBuilder.newBuilder().build();

  private SQLProvider provider;

  private final SQLProviderListener providerListener = new InternalSQLProviderListener();

  private EdgeCache(final SQLProvider provider) {
    this.provider = provider;
    this.provider.addListener(providerListener);
  }

  public static synchronized EdgeCache get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE01239: Provider argument can not be null");
    if (!caches.containsKey(provider)) {
      caches.put(provider, new EdgeCache(provider));
    }
    return caches.get(provider);
  }

  private void close() {
    caches.remove(provider);
    provider.removeListener(providerListener);
  }

  public void addEdges(final List<INaviEdge> edges) {
    final ImmutableMap<Integer, INaviEdge> edgesMap =
        Maps.uniqueIndex(edges, new Function<INaviEdge, Integer>() {
          @Override
          public Integer apply(final INaviEdge edge) {
            return edge.getId();
          }
        });

    edgesByIdCache.putAll(edgesMap);

    for (final INaviEdge edge : edges) {
      if (edge.getSource() instanceof IAddressNode && edge.getTarget() instanceof IAddressNode) {
        final IAddress sourceAddress = ((IAddressNode) edge.getSource()).getAddress();
        final IAddress targetAddress = ((IAddressNode) edge.getTarget()).getAddress();
        Integer sourceModuleId = null;
        Integer targetModuleId = null;
        if (edge.getSource() instanceof INaviCodeNode) {
          sourceModuleId = getModuleId((INaviCodeNode) edge.getSource());
        } else if (edge.getSource() instanceof INaviFunctionNode) {
          sourceModuleId = getModuleId((INaviFunctionNode) edge.getSource());
        }
        if (edge.getTarget() instanceof INaviCodeNode) {
          targetModuleId = getModuleId((INaviCodeNode) edge.getTarget());
        } else if (edge.getTarget() instanceof INaviFunctionNode) {
          targetModuleId = getModuleId((INaviFunctionNode) edge.getTarget());
        }
        if (targetModuleId != null && sourceModuleId != null) {
          UpdateAddressModuleIdCache(sourceAddress, sourceModuleId, targetAddress, targetModuleId,
              edge);
        }
      }
    }
  }

  private void UpdateAddressModuleIdCache(IAddress sourceAddress, Integer sourceModuleId,
      IAddress targetAddress, Integer targetModuleId, INaviEdge edge) {
    final EdgeIdentifier edgeAddressModuleIds =
        new EdgeIdentifier(sourceAddress, sourceModuleId, targetAddress, targetModuleId);
    List<Integer> edgeIds = edgeAddressToEdgeIdsCache.getIfPresent(edgeAddressModuleIds);
    if (edgeIds != null) {
      edgeIds.add(edge.getId());
    } else {
      edgeIds = Lists.newArrayList(edge.getId());
    }
    edgeAddressToEdgeIdsCache.put(edgeAddressModuleIds, edgeIds);
  }

  private Integer getModuleId(final INaviCodeNode node) {
    try {
      return node.getParentFunction().getModule().getConfiguration().getId();
    } catch (MaybeNullException e) {
      return null;
    }
  }

  private Integer getModuleId(final INaviFunctionNode node) {
    return node.getFunction().getModule().getConfiguration().getId();
  }

  public INaviEdge getEdgeById(final Integer edgeId) {
    return edgesByIdCache.getIfPresent(edgeId);
  }

  public ImmutableCollection<INaviEdge> getEdgeBySourceAndTarget(final IAddress sourceAddress,
      final Integer sourceModuleId, final IAddress targetAddress, final Integer targetModuleId) {
    final EdgeIdentifier edgeAddressModuleIds =
        new EdgeIdentifier(sourceAddress, sourceModuleId, targetAddress, targetModuleId);
    final List<Integer> edgeIds = edgeAddressToEdgeIdsCache.getIfPresent(edgeAddressModuleIds);
    return edgesByIdCache.getAllPresent(edgeIds).values();
  }

  /**
   * Internal listener class to keep informed about changes in the {@link SQLProvider provider}.
   */
  private class InternalSQLProviderListener implements SQLProviderListener {

    @Override
    public void providerClosing(SQLProvider provider) {
      if (EdgeCache.this.provider.equals(provider)) {
        EdgeCache.this.close();
      }
    }
  }
}
