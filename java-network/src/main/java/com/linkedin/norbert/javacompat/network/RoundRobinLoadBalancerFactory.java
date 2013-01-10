/*
 * Copyright 2009-2010 LinkedIn, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.linkedin.norbert.javacompat.network;

import com.linkedin.norbert.EndpointConversions;
import com.linkedin.norbert.cluster.InvalidClusterException;
import com.linkedin.norbert.javacompat.cluster.Node;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancerFactory implements LoadBalancerFactory {
    private final com.linkedin.norbert.network.client.loadbalancer.RoundRobinLoadBalancerFactory scalaLbf =
            new com.linkedin.norbert.network.client.loadbalancer.RoundRobinLoadBalancerFactory();

    final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public LoadBalancer newLoadBalancer(final Set<Endpoint> endpoints) throws InvalidClusterException {
      final com.linkedin.norbert.network.client.loadbalancer.LoadBalancer loadBalancer =
        scalaLbf.newLoadBalancer(EndpointConversions.convertJavaEndpointSet(endpoints));

      return new LoadBalancer() {
        @Override
        public Node nextNode() {
          return nextNode(0L);
        }

        @Override
        public Node nextNode(Long capability){

            ArrayList<Endpoint> activeEndpoints = new ArrayList<Endpoint>();
            for(Endpoint endpoint : endpoints){
                if (endpoint.canServeRequests() && endpoint.getNode().isCapableOf(capability)){
                    activeEndpoints.add(endpoint);
                }
            }

            if(endpoints.isEmpty()){
                return null;
            }else{
                return activeEndpoints.get(Math.abs(counter.getAndIncrement()) % activeEndpoints.size()).getNode();
            }

        }

      };
    }
}
