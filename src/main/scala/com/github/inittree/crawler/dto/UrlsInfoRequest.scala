package com.github.inittree.crawler.dto

import cats.effect.Concurrent
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

final case class UrlsInfoRequest(urls: List[String])

object UrlsInfoRequest {

  implicit val urlsInfoRequestDecoder: Decoder[UrlsInfoRequest] = deriveDecoder[UrlsInfoRequest]
  implicit def urlsInfoRequestEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, UrlsInfoRequest] =
    jsonOf

}
