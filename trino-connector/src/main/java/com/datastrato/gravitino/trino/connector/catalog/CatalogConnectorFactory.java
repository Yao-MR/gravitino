/*
 * Copyright 2023 Datastrato.
 * This software is licensed under the Apache License version 2.
 */
package com.datastrato.gravitino.trino.connector.catalog;

import static com.datastrato.gravitino.trino.connector.GravitinoErrorCode.GRAVITINO_CREATE_INTERNAL_CONNECTOR_ERROR;
import static com.datastrato.gravitino.trino.connector.GravitinoErrorCode.GRAVITINO_UNSUPPORTED_CATALOG_PROVIDER;

import com.datastrato.gravitino.NameIdentifier;
import com.datastrato.gravitino.client.GravitinoMetaLake;
import com.datastrato.gravitino.trino.connector.catalog.hive.HiveConnectorAdapter;
import com.datastrato.gravitino.trino.connector.catalog.memory.MemoryConnectorAdapter;
import com.datastrato.gravitino.trino.connector.metadata.GravitinoCatalog;
import io.trino.spi.TrinoException;
import io.trino.spi.connector.Connector;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class use to create CatalogConnectorContext instance by given catalog. */
public class CatalogConnectorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(CatalogConnectorFactory.class);

  private final CatalogInjector catalogInjector;
  private final HashMap<String, CatalogConnectorContext.Builder> catalogBuilders = new HashMap<>();

  public CatalogConnectorFactory(CatalogInjector catalogInjector) {
    this.catalogInjector = catalogInjector;

    catalogBuilders.put("hive", new CatalogConnectorContext.Builder(new HiveConnectorAdapter()));
    catalogBuilders.put(
        "memory", new CatalogConnectorContext.Builder(new MemoryConnectorAdapter()));
  }

  public CatalogConnectorContext loadCatalogConnector(
      NameIdentifier nameIdentifier, GravitinoMetaLake metalake, GravitinoCatalog catalog) {
    String catalogProvider = catalog.getProvider();
    CatalogConnectorContext.Builder builder = catalogBuilders.get(catalogProvider);
    if (builder == null) {
      String message = String.format("Unsupported catalog provider %s.", catalogProvider);
      LOG.error(message);
      throw new TrinoException(GRAVITINO_UNSUPPORTED_CATALOG_PROVIDER, message);
    }

    // Avoid using the same builder object to prevent catalog creation errors.
    builder = builder.clone();

    try {
      Connector internalConnector =
          catalogInjector.createConnector(nameIdentifier.toString(), builder.buildConfig(catalog));

      return builder
          .withMetalake(metalake)
          .withCatalogName(nameIdentifier)
          .withInternalConnector(internalConnector)
          .build();

    } catch (Exception e) {
      String message =
          String.format("Failed to create internal catalog connector. The catalog is: %s", catalog);
      LOG.error(message, e);
      throw new TrinoException(GRAVITINO_CREATE_INTERNAL_CONNECTOR_ERROR, message, e);
    }
  }
}