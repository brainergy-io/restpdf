package io.brainergy.pdf.kek

import com.itextpdf.signatures.IExternalSignature
import com.typesafe.scalalogging.LazyLogging
import io.brainergy.util.{B64, Conf}
import scalaj.http.Http

import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

case class RestSignature(masterAlias: String
                         , masterPassword: String
                         , kek: String
                         , hash: String = "SHA-256"
                         , algo: String = "RSA")
  extends IExternalSignature
    with B64
    with LazyLogging {

  private var activeURL = 0

  override def sign(message: Array[Byte]): Array[Byte] = {
    val body =
      s"""
         |master_alias=$masterAlias
         |master_password=$masterPassword
         |kek=$kek
         |message=${encoder.encodeToString(message)}
         |""".stripMargin

    logger.debug(body)

    retry(4) {
      logger.info(s"Signer on : ${uri()}")
      val response = Http(s"${uri()}/rsa/kek/sign/msg")
        .header("X-Access-Token", TOKEN)
        .postData(body)
        .asString
        .body

      decoder.decode(response)
    }

  }

  @annotation.tailrec
  final def retry[A](limit: Int
                     , count: Int = 1
                     , backoff: Duration = 5 seconds)(f: => A): A = Try(f) match {
    case Success(x) => x

    case Failure(e) =>
      logger.error(e.getMessage, e.getCause)
      activeURL = if (activeURL < HSM_URLS.size - 1) activeURL + 1 else 0
      if (count < limit) {
        Thread.sleep(backoff.toMillis)
        retry(limit, count + 1, backoff)(f)
      } else {
        throw new RuntimeException(s"Reach Retry limit $count times", e)
      }
  }

  private def uri() = HSM_URLS(activeURL)

  override def getHashAlgorithm: String = hash

  override def getEncryptionAlgorithm: String = algo

  private lazy val HSM_URLS = Conf("hsm.url").split(",")
  private lazy val TOKEN = Conf("hsm.token")

}
