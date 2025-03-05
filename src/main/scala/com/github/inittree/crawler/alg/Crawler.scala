package com.github.inittree.crawler.alg

import cats.Parallel
import cats.effect.{Async, Sync}
import cats.implicits._
import com.github.inittree.crawler.dto.UrlsInfoResponse
import org.http4s.client.Client
import org.http4s.{Request, Response, Uri}
import org.jsoup.Jsoup

trait Crawler[F[_]] {
  def fetchUrlsInfo(urls: List[String]): F[List[UrlsInfoResponse]]
}

object Crawler {

  def impl[F[_]: Async: Parallel](client: Client[F]): Crawler[F] = new Crawler[F] {

    override def fetchUrlsInfo(urls: List[String]): F[List[UrlsInfoResponse]] = {
      urls.parTraverse { url => fetchUrlInfo(client, url) }
    }

    private def fetchUrlInfo(client: Client[F], url: String): F[UrlsInfoResponse] = {
      Sync[F].handleErrorWith(
        for {
          uri <- Sync[F].fromEither(Uri.fromString(url))
          request = Request[F](uri = uri)
          result <- client.run(request).use { response =>
            processResponse(url, response)
          }
        } yield result
      ) { error =>
        Sync[F].pure(UrlsInfoResponse(url, None, Some(s"Fetch error: ${error.getMessage}")))
      }
    }

    private def processResponse(url: String, response: Response[F]): F[UrlsInfoResponse] = {
      if (response.status.isSuccess) {
        response.bodyText.compile.string.map { body =>
          val doc   = Jsoup.parse(body)
          val title = Option(doc.title()).filter(_.nonEmpty)
          UrlsInfoResponse(url, title, None)
        }
      } else {
        Sync[F].pure(UrlsInfoResponse(url, None, Some(s"HTTP Error: ${response.status}")))
      }
    }
  }
}
