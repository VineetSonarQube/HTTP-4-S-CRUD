package com.example.quickstart

import cats.effect.Async
import cats.syntax.all._
import com.comcast.ip4s._
import com.example.quickstart.repositories.UserRepo
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object QuickstartServer {

  def run[F[_] : Async : Network]: F[Nothing] = {

    for {
      client <- EmberClientBuilder.default[F].build
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)
      uuidAlg = UUIDs.impl[F]
      userAlg = new UserRepo[F]
      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.
      httpApp = (
        QuickstartRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          QuickstartRoutes.jokeRoutes[F](jokeAlg) <+>
          QuickstartRoutes.uuidRoute[F](uuidAlg) <+>
          QuickstartRoutes.userRoute[F](userAlg)
        ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <-
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
