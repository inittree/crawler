package com.github.inittree.crawler

import cats.effect.Concurrent
import cats.implicits._
import com.github.inittree.crawler.alg.Crawler
import com.github.inittree.crawler.dto.UrlsInfoRequest
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object CrawlerRoutes {
  def crawlerRoutes[F[_]: Concurrent](crawler: Crawler[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "fetchUrlsInfos" =>
        req.as[UrlsInfoRequest].attempt.flatMap {
          case Right(urlsRequest) =>
            crawler.fetchUrlsInfo(urlsRequest.urls).flatMap(urlInfos => Ok(urlInfos.asJson))
          case Left(e) =>
            BadRequest(s"Incorrect request: ${e.getMessage}")
        }
    }
  }
}
