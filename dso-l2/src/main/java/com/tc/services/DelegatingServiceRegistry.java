package com.tc.services;


import com.google.common.collect.ImmutableMap;
import com.tc.util.Assert;

import org.terracotta.entity.ServiceConfiguration;
import org.terracotta.entity.ServiceProvider;
import org.terracotta.entity.ServiceRegistry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DelegatingServiceRegistry implements ServiceRegistry {
  private final long consumerID;
  private final Map<Class<?>, List<ServiceProvider>> serviceProviderMap;

  public DelegatingServiceRegistry(long consumerID, ServiceProvider[] providers) {
    this.consumerID = consumerID;
    Map<Class<?>, List<ServiceProvider>> temp = new HashMap<>();
    for(ServiceProvider provider : providers) {
      for (Class<?> serviceType : provider.getProvidedServiceTypes()) {
        List<ServiceProvider> listForType = temp.get(serviceType);
        if (null == listForType) {
          listForType = new LinkedList<>();
          temp.put(serviceType, listForType);
        }
        listForType.add(provider);
      }
    }
    serviceProviderMap = ImmutableMap.copyOf(temp);
  }


  @Override
  public <T> T getService(ServiceConfiguration<T> configuration) {
    List<ServiceProvider> serviceProviders = serviceProviderMap.get(configuration.getServiceType());
    if (serviceProviders == null) {
     return null;
    }
    T service = null;
    for (ServiceProvider provider : serviceProviders) {
      T oneService = provider.getService(this.consumerID, configuration);
      if (null != oneService) {
        // TODO:  Determine how to rationalize multiple matches.  For now, we will force either 1 or 0.
        Assert.assertNull(service);
        service = oneService;
      }
    }
    return service;
  }
}