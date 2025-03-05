package com.github.inittree.crawler

import cats.Parallel
import cats.effect.Async
import com.comcast.ip4s._
import com.github.inittree.crawler.alg.Crawler
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object CrawlerServer {

  def run[F[_]: Async: Network: Parallel]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build
      crawlerAlg = Crawler.impl(client)

      httpApp = CrawlerRoutes.crawlerRoutes[F](crawlerAlg)
        .orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      _ <-
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
