package v1.providers

import com.datastax.driver.core.{Cluster, Session}

import javax.inject.Singleton

@Singleton
object CassandraProvider {
  private val cluster = Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9042)
    .build()

  val session: Session = cluster.connect()
}
