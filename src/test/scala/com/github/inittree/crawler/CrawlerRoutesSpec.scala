package com.github.inittree.crawler

import cats.effect.IO
import com.github.inittree.crawler.alg.Crawler
import com.github.inittree.crawler.dto.UrlsInfoResponse
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._

class CrawlerRoutesSpec extends CatsEffectSuite {

  val stubTitle = "Stub Title"

  val stubCrawler = new Crawler[IO] {
    override def fetchUrlsInfo(urls: List[String]): IO[List[UrlsInfoResponse]] =
      IO.pure(urls.map(url => UrlsInfoResponse(url, Some(stubTitle), None)))
  }

  val routes: HttpApp[IO] = CrawlerRoutes.crawlerRoutes[IO](stubCrawler).orNotFound

  test("POST /fetchUrlsInfos returns correct response for valid JSON") {
    val jsonRequest = """{ "urls": ["http://example.com"] }"""
    val request = Request[IO](method = Method.POST, uri = uri"/fetchUrlsInfos")
      .withEntity(jsonRequest)
    for {
      response <- routes.run(request)
      body     <- response.as[String]
    } yield {
      assertEquals(response.status, Status.Ok)
      val parsed = io.circe.jawn.parse(body).getOrElse(Json.Null)
      val title  = parsed.hcursor.downArray.get[String]("title").getOrElse("")
      assertEquals(title, stubTitle)
    }
  }

  test("POST /fetchUrlsInfos returns 400 for invalid JSON") {
    val invalidJsonRequest = """{ "wrongField": "value" }"""
    val request = Request[IO](method = Method.POST, uri = uri"/fetchUrlsInfos")
      .withEntity(invalidJsonRequest)
    for {
      response <- routes.run(request)
      body     <- response.as[String]
    } yield {
      assertEquals(response.status, Status.BadRequest)
      assert(body.contains("Incorrect request"))
    }
  }
}
