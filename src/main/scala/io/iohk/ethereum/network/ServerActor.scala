package io.iohk.ethereum.network

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{Bind, Bound, CommandFailed, Connected}
import akka.io.{IO, Tcp}
import io.iohk.ethereum.utils.{NodeStatus, ServerStatus}
import java.util.concurrent.atomic.AtomicReference
import org.bouncycastle.util.encoders.Hex

class ServerActor(nodeStatusHolder: AtomicReference[NodeStatus], peerManager: ActorRef)
    extends Actor
    with ActorLogging {

  import ServerActor._
  import context.system

  override def receive: Receive = { case StartServer(address) =>
    IO(Tcp) ! Bind(self, address)
    context become waitingForBindingResult
  }

  def waitingForBindingResult: Receive = {
    case Bound(localAddress) =>
      val nodeStatus = nodeStatusHolder.get()
      log.info("Listening on {}", localAddress)
      log.info(
        "Node address: enode://{}@{}:{}",
        Hex.toHexString(nodeStatus.nodeId),
        getHostName(localAddress.getAddress),
        localAddress.getPort
      )
      nodeStatusHolder.getAndUpdate(_.copy(serverStatus = ServerStatus.Listening(localAddress)))
      context become listening

    case CommandFailed(b: Bind) =>
      log.warning("Binding to {} failed", b.localAddress)
      context stop self
  }

  def listening: Receive = { case Connected(remoteAddress, _) =>
    val connection = sender()
    peerManager ! PeerManagerActor.HandlePeerConnection(connection, remoteAddress)
  }
}

object ServerActor {
  def props(nodeStatusHolder: AtomicReference[NodeStatus], peerManager: ActorRef): Props =
    Props(new ServerActor(nodeStatusHolder, peerManager))

  case class StartServer(address: InetSocketAddress)
}
