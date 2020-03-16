package rocks.metaldetector.butler.config.resttemplate

import org.apache.http.HeaderElement
import org.apache.http.HeaderElementIterator
import org.apache.http.HeaderIterator
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.apache.http.conn.ConnectionKeepAliveStrategy
import org.apache.http.conn.routing.HttpRoute
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicHeaderElementIterator
import org.apache.http.protocol.HTTP
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

import java.util.concurrent.TimeUnit

import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.CONNECTION_TIMEOUT
import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.DEFAULT_KEEP_ALIVE_TIME
import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.IDLE_CONNECTION_WAIT_TIME
import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.MAX_TOTAL_CONNECTIONS
import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.MAX_ROUTE_CONNECTIONS
import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.MAX_LOCALHOST_CONNECTIONS
import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.REQUEST_TIMEOUT
import static rocks.metaldetector.butler.config.resttemplate.HttpClientConfigConstants.SOCKET_TIMEOUT

@Configuration
class ApacheHttpClientConfig {

  final Logger log = LoggerFactory.getLogger(ApacheHttpClientConfig)
  final int port

  ApacheHttpClientConfig(@Value(value = '${server.port}') int port) {
    this.port = port
  }

  @Bean
  PoolingHttpClientConnectionManager poolingConnectionManager() {
    PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager()

    // set total amount of connections across all HTTP routes
    poolingConnectionManager.maxTotal = MAX_TOTAL_CONNECTIONS

    // set maximum amount of connections for each http route in pool
    poolingConnectionManager.defaultMaxPerRoute = MAX_ROUTE_CONNECTIONS

    // increase the amounts of connections if host is localhost
    HttpHost localhost = new HttpHost("http://localhost", port)
    poolingConnectionManager.setMaxPerRoute(new HttpRoute(localhost), MAX_LOCALHOST_CONNECTIONS)

    return poolingConnectionManager
  }

  @Bean
  ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
    return { httpResponse, httpContext ->
      HeaderIterator headerIterator = httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE)
      HeaderElementIterator elementIterator = new BasicHeaderElementIterator(headerIterator)

      while (elementIterator.hasNext()) {
        HeaderElement element = elementIterator.nextElement()
        String param = element.name
        String value = element.value
        if (value != null && param.equalsIgnoreCase("timeout")) {
          return Long.parseLong(value) * 1000 // convert to ms
        }
      }

      return DEFAULT_KEEP_ALIVE_TIME
    }
  }

  @Bean
  Runnable idleConnectionMonitor(PoolingHttpClientConnectionManager pool) {
    return new Runnable() {
      @Override
      @Scheduled(fixedDelay = 60000L)
      void run() {
        // only if connection pool is initialised
        if (pool != null) {
          pool.closeExpiredConnections()
          pool.closeIdleConnections(IDLE_CONNECTION_WAIT_TIME, TimeUnit.MILLISECONDS)
        }
      }
    }
  }

  @Bean
  TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler()
    scheduler.threadNamePrefix = "idleMonitor"
    scheduler.poolSize = 5
    return scheduler
  }

  @Bean
  CloseableHttpClient httpClient() {
    RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setSocketTimeout(SOCKET_TIMEOUT)
            .build()

    return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(poolingConnectionManager())
            .setKeepAliveStrategy(connectionKeepAliveStrategy())
            .build()
  }

}
