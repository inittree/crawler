package com.github.inittree.crawler

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = CrawlerServer.run[IO]
}
