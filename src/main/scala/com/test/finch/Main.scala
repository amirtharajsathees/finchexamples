package com.test.finch

import cats.effect.IO
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
import io.circe.generic.auto._

object Main extends App {

  case class Message(name: String, age : Int, city: String)

  def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("OK")
  }

  def helloWorld: Endpoint[IO, Message] = get("hello") {
      Ok(Message("Patrick", 25,"Phily"))

  }
  // HTTP Get with multiple parameters
  def test: Endpoint[IO, Message] = get("test":: param[Int]("age"):: param[String](name = "city")) { (age: Int, city : String) =>
    Ok(Message("Alex", age, city))
  }
  // HTTP Get with multiple paths
  def hello: Endpoint[IO, Message] = get("hello" :: path[String] :: path[Int]) { (s: String, age: Int) =>
    Ok(Message(s, age, "Pittsburg"))
  }

  def service: Service[Request, Response] = Bootstrap
    .serve[Text.Plain](healthcheck)
    .serve[Application.Json](helloWorld :+: hello :+: test)
    .toService

  Await.ready(Http.server.serve(":8090", service))

  // Test REST Calls
  // http://localhost:8090/
  // http://localhost:8090/hello
  // http://localhost:8090/test?age=35&city=Phily
  // http://localhost:8090/hello/Alex
}