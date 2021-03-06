package in.tuxdna.services

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import in.tuxdna.db.Database


object StockAPI extends App {

  implicit val system = ActorSystem("Streams")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val routes = {
    logRequestResult("StockAPI") {
      pathPrefix("getAllTickers") {
        get {
          complete {
            for {
              input <- Database.findAllTickers
            } yield {
              input.toString
            }
          }
        }
      } ~
        pathPrefix("ticker") {
          (get & path(Segment)) { id =>
            complete {
              // query the database
              val tickerOption = Database.findTicker(id)
              for (t <- tickerOption) yield {
                t match {
                  case Some(t) => OK -> t.toString
                  case None => NotFound -> "No such ticker"
                }
              }
            }
          }
        }
    }
  }

  Http().bindAndHandle(routes, config.getString("stockAPI.http.interface"), config.getInt("stockAPI.http.port"))
}
