package rocks.metaldetector.butler.config.web

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
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

import java.time.Duration

import static java.util.concurrent.TimeUnit.MINUTES

@Configuration
class ApacheHttpClientConfig {

  private static final int MAX_ROUTE_CONNECTIONS = 2
  private static final int MAX_TOTAL_CONNECTIONS = 5
  private static final int MAX_LOCALHOST_CONNECTIONS = 10

  @Bean
  PoolingHttpClientConnectionManager poolingConnectionManager(@Value('${server.port}') int port) {
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
        if (value && param.equalsIgnoreCase("timeout")) {
          return Long.parseLong(value) * 1000 // convert to ms
        }
      }

      return Duration.ofSeconds(20).toMillis()
    }
  }

  @Bean
  Runnable idleConnectionMonitor(PoolingHttpClientConnectionManager pool) {
    return new Runnable() {

      @Override
      @Scheduled(fixedDelay = 60000L)
      void run() {
        // only if connection pool is initialised
        if (pool) {
          pool.closeExpiredConnections()
          pool.closeIdleConnections(10, MINUTES)
        }
      }
    }
  }

  @Bean
  CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingConnectionManager,
                                 ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(Duration.ofSeconds(20).toMillis() as int) // the time for waiting until a connection is established
        .setConnectionRequestTimeout(Duration.ofSeconds(20).toMillis() as int) // the time for waiting for a connection from connection pool
        .setSocketTimeout(Duration.ofSeconds(30).toMillis() as int) // the time for waiting for data
        .build()

    return HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .setConnectionManager(poolingConnectionManager)
        .setKeepAliveStrategy(connectionKeepAliveStrategy)
        .build()
  }
}
