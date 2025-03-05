package com.github.inittree.crawler

import cats.effect.IO
import com.github.inittree.crawler.alg.Crawler
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.implicits._

class CrawlerSpec extends CatsEffectSuite {

  test("fetchUrlsInfo returns title if response is successful") {
    val htmlResponse = "<html><head><title>Test Title</title></head><body></body></html>"
    val routes = HttpRoutes.of[IO] {
      case _ => Ok(htmlResponse)
    }
    val client: Client[IO] = Client.fromHttpApp(routes.orNotFound)
    val crawler            = Crawler.impl[IO](client)

    for {
      responses <- crawler.fetchUrlsInfo(List("http://example.com"))
    } yield {
      assertEquals(responses.length, 1)
      val response = responses.head
      assertEquals(response.title, Some("Test Title"))
      assertEquals(response.error, None)
    }
  }

  test("fetchUrlsInfo returns error if response is not successful") {
    val routes = HttpRoutes.of[IO] {
      case _ => NotFound("Not found")
    }
    val client: Client[IO] = Client.fromHttpApp(routes.orNotFound)
    val crawler            = Crawler.impl[IO](client)

    for {
      responses <- crawler.fetchUrlsInfo(List("http://example.com"))
    } yield {
      assertEquals(responses.length, 1)
      val response = responses.head
      assert(response.error.exists(_.contains("HTTP Error")))
      assertEquals(response.title, None)
    }
  }

  test("fetchUrlsInfo возвращает ошибку, если URL невалидный") {
    val client: Client[IO] = Client.fromHttpApp(HttpRoutes.empty[IO].orNotFound)
    val crawler            = Crawler.impl[IO](client)

    for {
      responses <- crawler.fetchUrlsInfo(List("not-valid-url"))
    } yield {
      assertEquals(responses.length, 1)
      val response = responses.head
      assertEquals(response.title, None)
      assert(response.error.nonEmpty)
    }
  }
}
