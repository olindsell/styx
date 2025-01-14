/*
  Copyright (C) 2013-2023 Expedia Inc.

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
package com.hotels.styx.server.netty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hotels.styx.server.ConnectorConfig;
import com.hotels.styx.server.HttpConnectorConfig;
import com.hotels.styx.server.HttpsConnectorConfig;

import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

/**
 * Configuration values for the netty based web server.
 *
 */
@JsonDeserialize(builder = NettyServerConfig.Builder.class)
public class NettyServerConfig {
    public static final int HALF_OF_AVAILABLE_PROCESSORS = max(1, getRuntime().availableProcessors() / 2);

    private int bossThreadsCount = 1;
    private int workerThreadsCount = HALF_OF_AVAILABLE_PROCESSORS;

    private int nioAcceptorBacklog = 1024;
    private int maxInitialLength = 4096;
    private int maxHeaderSize = 8192;

    @Deprecated
    private int maxChunkSize = Integer.MAX_VALUE;
    private int requestTimeoutMs = 12000;
    private int keepAliveTimeoutMillis = 12000;
    private int maxConnectionsCount = 512;
    private boolean compressResponses;

    private final Optional<HttpConnectorConfig> httpConnectorConfig;
    private final Optional<HttpsConnectorConfig> httpsConnectorConfig;

    private final Iterable<ConnectorConfig> connectors;

    public NettyServerConfig() {
        this.httpConnectorConfig = Optional.of(new HttpConnectorConfig(8080));
        this.httpsConnectorConfig = Optional.empty();
        this.connectors = singleton(httpConnectorConfig.get());
    }

    protected NettyServerConfig(Builder<?> builder) {
        this.bossThreadsCount = builder.bossThreadsCount;
        this.workerThreadsCount = builder.workerThreadsCount;
        this.nioAcceptorBacklog = builder.nioAcceptorBacklog;
        this.maxInitialLength = builder.maxInitialLength;
        this.maxHeaderSize = builder.maxHeaderSize;
        this.maxChunkSize = builder.maxChunkSize;
        this.requestTimeoutMs = builder.requestTimeoutMs;
        this.keepAliveTimeoutMillis = builder.keepAliveTimeoutMillis;
        this.maxConnectionsCount = builder.maxConnectionsCount;

        this.httpConnectorConfig = Optional.ofNullable(builder.httpConnectorConfig);
        this.httpsConnectorConfig = Optional.ofNullable(builder.httpsConnectorConfig);
        this.compressResponses = builder.compressResponses;
        this.connectors = connectorsIterable();
    }

    private Iterable<ConnectorConfig> connectorsIterable() {
        return Stream.of(httpConnectorConfig, httpsConnectorConfig)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public Optional<HttpConnectorConfig> httpConnectorConfig() {
        return httpConnectorConfig;
    }

    public Optional<HttpsConnectorConfig> httpsConnectorConfig() {
        return httpsConnectorConfig;
    }

    public Iterable<ConnectorConfig> connectors() {
        return connectors;
    }

    /**
     * Number of threads for establishing new channels.
     *
     * @return number of threads
     */
    public int bossThreadsCount() {
        return this.bossThreadsCount;
    }

    /**
     * Worker threads are those performing all the asynchronous I/O operation on the inbound channel.
     *
     * @return number of threads
     */
    public int workerThreadsCount() {
        return this.workerThreadsCount;
    }

    public int nioAcceptorBacklog() {
        return this.nioAcceptorBacklog;
    }

    /**
     * The maximum length in bytes of the initial line of an HTTP message, e.g. {@code GET http://example.org/ HTTP/1.1}.
     *
     * @return maximum length of initial line
     */
    public int maxInitialLength() {
        return this.maxInitialLength;
    }

    /**
     * The maximum combined size of the HTTP headers in bytes.
     *
     * @return maximum combined size of headers
     */
    public int maxHeaderSize() {
        return this.maxHeaderSize;
    }

    /**
     * The maximum size of an HTTP chunk in bytes.
     *
     * @return maximum chunk size
     */
    @Deprecated
    public int maxChunkSize() {
        return this.maxChunkSize;
    }

    /**
     * This parameter controls the amount of tolerated inactivity while the request is being received.
     * If a client started sending a request, and then suddenly cuts it off, Styx would detect this after this
     * duration has elapsed.
     *
     * @return time in millis
     */
    public int requestTimeoutMillis() {
        return this.requestTimeoutMs;
    }

    /**
     * A timeout for idle persistent connections, in milliseconds.
     *
     * @return time in millis
     */
    public int keepAliveTimeoutMillis() {
        return this.keepAliveTimeoutMillis;
    }

    /**
     * Max connections to server before we start rejecting them.
     *
     * @return max number of connections
     */
    public int maxConnectionsCount() {
        return this.maxConnectionsCount;
    }

    /**
     * Whether responses should be compressed.
     *
     * @return true if response compression is enabled
     */
    public boolean compressResponses() {
        return compressResponses;
    }

    /**
     * Builder.
     *
     * @param <T> the type of the Builder
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder<T extends Builder<T>> {
        protected int bossThreadsCount = HALF_OF_AVAILABLE_PROCESSORS;
        protected int workerThreadsCount = HALF_OF_AVAILABLE_PROCESSORS;
        protected int nioAcceptorBacklog = 1024;
        protected int maxInitialLength = 4096;
        protected int maxHeaderSize = 8192;
        protected int maxChunkSize = Integer.MAX_VALUE;
        protected int requestTimeoutMs = 12000;
        protected int keepAliveTimeoutMillis = 12000;
        protected int maxConnectionsCount = 512;
        protected HttpConnectorConfig httpConnectorConfig;
        protected HttpsConnectorConfig httpsConnectorConfig;
        protected boolean compressResponses;

        public Builder httpPort(int port) {
            return (T) setHttpConnector(new HttpConnectorConfig(port));
        }

        @JsonProperty("bossThreadsCount")
        public T setBossThreadsCount(Integer bossThreadsCount) {
            if (bossThreadsCount != null) {
                this.bossThreadsCount = bossThreadsCount;
            }
            return (T) this;
        }

        @JsonProperty("workerThreadsCount")
        public T setWorkerThreadsCount(Integer workerThreadsCount) {
            if (workerThreadsCount != null && workerThreadsCount > 0) {
                this.workerThreadsCount = workerThreadsCount;
            }
            return (T) this;
        }

        @JsonProperty("nioAcceptorBacklog")
        public T setNioAcceptorBacklog(Integer nioAcceptorBacklog) {
            if (nioAcceptorBacklog != null) {
                this.nioAcceptorBacklog = nioAcceptorBacklog;
            }
            return (T) this;
        }

        @JsonProperty("maxInitialLength")
        public T setMaxInitialLength(Integer maxInitialLength) {
            if (maxInitialLength != null) {
                this.maxInitialLength = maxInitialLength;
            }
            return (T) this;
        }

        @JsonProperty("maxHeaderSize")
        public T setMaxHeaderSize(Integer maxHeaderSize) {
            if (maxHeaderSize != null) {
                this.maxHeaderSize = maxHeaderSize;
            }
            return (T) this;
        }

        @Deprecated
        @JsonProperty("maxChunkSize")
        public T setMaxChunkSize(Integer maxChunkSize) {
            if (maxChunkSize != null) {
                this.maxChunkSize = maxChunkSize;
            }
            return (T) this;
        }

        @JsonProperty("requestTimeoutMillis")
        public T setRequestTimeoutMs(Integer requestTimeoutMs) {
            if (requestTimeoutMs != null) {
                this.requestTimeoutMs = requestTimeoutMs;
            }
            return (T) this;
        }

        @JsonProperty("keepAliveTimeoutMillis")
        public T setKeepAliveTimeoutMillis(Integer keepAliveTimeoutMillis) {
            if (keepAliveTimeoutMillis != null) {
                this.keepAliveTimeoutMillis = keepAliveTimeoutMillis;
            }
            return (T) this;
        }

        @JsonProperty("connectors")
        public T setConnectors(Connectors connectors) {
            this.httpConnectorConfig = connectors.http;
            this.httpsConnectorConfig = connectors.https;
            return (T) this;
        }

        public T setHttpConnector(HttpConnectorConfig httpConnector) {
            this.httpConnectorConfig = httpConnector;
            return (T) this;
        }

        public T setHttpsConnector(HttpsConnectorConfig httpsConnector) {
            this.httpsConnectorConfig = httpsConnector;
            return (T) this;
        }

        @JsonProperty("maxConnectionsCount")
        public T setMaxConnectionsCount(Integer maxConnectionsCount) {
            if (maxConnectionsCount != null) {
                this.maxConnectionsCount = maxConnectionsCount;
            }
            return (T) this;
        }

        @JsonProperty("compressResponses")
        public T setCompressResponses(boolean compressResponses) {
            this.compressResponses = compressResponses;
            return (T) this;
        }

        public NettyServerConfig build() {
            return new NettyServerConfig(this);
        }
    }

    /**
     * Connectors.
     */
    public static class Connectors {
        private final HttpConnectorConfig http;
        private final HttpsConnectorConfig https;

        public Connectors(@JsonProperty("http") HttpConnectorConfig http,
                          @JsonProperty("https") HttpsConnectorConfig https) {
            this.http = http;
            this.https = https;
        }
    }
}
