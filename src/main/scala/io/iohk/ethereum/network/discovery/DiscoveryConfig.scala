package io.iohk.ethereum.network.discovery

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

case class DiscoveryConfig(
    discoveryEnabled: Boolean,
    host: String,
    interface: String,
    port: Int,
    bootstrapNodes: Set[Node],
    reuseKnownNodes: Boolean,
    scanInterval: FiniteDuration,
    messageExpiration: FiniteDuration,
    maxClockDrift: FiniteDuration,
    requestTimeout: FiniteDuration,
    kademliaTimeout: FiniteDuration,
    kademliaBucketSize: Int,
    channelCapacity: Int
)

object DiscoveryConfig {
  def apply(etcClientConfig: com.typesafe.config.Config, bootstrapNodes: Set[String]): DiscoveryConfig = {
    val discoveryConfig = etcClientConfig.getConfig("network.discovery")

    DiscoveryConfig(
      discoveryEnabled = discoveryConfig.getBoolean("discovery-enabled"),
      host = discoveryConfig.getString("host"),
      interface = discoveryConfig.getString("interface"),
      port = discoveryConfig.getInt("port"),
      bootstrapNodes = NodeParser.parseNodes(bootstrapNodes),
      reuseKnownNodes = discoveryConfig.getBoolean("use-known-nodes"),
      scanInterval = discoveryConfig.getDuration("scan-interval").toMillis.millis,
      messageExpiration = discoveryConfig.getDuration("message-expiration").toMillis.millis,
      maxClockDrift = discoveryConfig.getDuration("max-clock-drift").toMillis.millis,
      requestTimeout = discoveryConfig.getDuration("request-timeout").toMillis.millis,
      kademliaTimeout = discoveryConfig.getDuration("kademlia-timeout").toMillis.millis,
      kademliaBucketSize = discoveryConfig.getInt("kademlia-bucket-size"),
      channelCapacity = discoveryConfig.getInt("channel-capacity")
    )
  }

}
