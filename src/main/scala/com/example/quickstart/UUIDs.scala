package com.example.quickstart

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

import java.util.UUID

trait UUIDs[F[_]] {
  def get: F[UUIDs.Create]

  def getAll(no: Int): F[List[UUIDs.Create]]
}

object UUIDs {
  final case class Create(uuid: UUID)

  object Create {
    implicit val UUIDEncoded: Encoder[Create] = (a: Create) => Json.obj(("UUID", Json.fromString(a.uuid.toString)))

    implicit def UUIDEntityEncoder[F[_]]: EntityEncoder[F, Create] = jsonEncoderOf[F, Create]

    implicit def UUIDsEntityEncoder[F[_]]: EntityEncoder[F, List[Create]] = jsonEncoderOf[F, List[Create]]
  }

  def impl[F[_] : Applicative]: UUIDs[F] = new UUIDs[F] {
    override def get: F[Create] = Create(UUID.randomUUID()).pure[F]

    override def getAll(no: Int): F[List[Create]] = getUUIDs(no).pure[F]
  }

  def getUUIDs(no: Int): List[Create] =
    List.fill(no)(Create(UUID.randomUUID()))
}