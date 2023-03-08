package rocks.metaldetector.butler.config.web

import org.apache.hc.client5.http.HttpRoute
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.util.TimeValue
import org.apache.hc.core5.util.Timeout
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

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
  Runnable idleConnectionMonitor(PoolingHttpClientConnectionManager pool) {
    return new Runnable() {

      @Override
      @Scheduled(fixedDelay = 60000L)
      void run() {
        // only if connection pool is initialised
        if (pool) {
          pool.closeExpired()
          pool.closeIdle(TimeValue.of(10, MINUTES))
        }
      }
    }
  }

  @Bean
  CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingConnectionManager) {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(Timeout.ofSeconds(20)) // the time for waiting until a connection is established
        .setConnectionRequestTimeout(Timeout.ofSeconds(20)) // the time for waiting for a connection from connection pool
        .setResponseTimeout(Timeout.ofSeconds(20)) // the time for waiting for data
        .build()

    return HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .setConnectionManager(poolingConnectionManager)
        .build()
  }
}
