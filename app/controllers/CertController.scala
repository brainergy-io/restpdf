package controllers

import com.typesafe.scalalogging.LazyLogging
import io.brainergy.cert.CertFacade
import io.brainergy.util.{B64, BJson}
import play.api.libs.Files
import play.api.mvc.{Action, MultipartFormData}

import java.util.Base64
import javax.inject.{Inject, Singleton}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class CertController @Inject()(facade: CertFacade)
  extends BasedController
    with LazyLogging
    with BJson
    with B64 {

  def save: Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData) { implicit request =>

    if (hasAuthorized())
      doSave(request.body.dataParts)
    else
      Unauthorized("Token Invalid")
  }

  def get: Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData) { implicit request =>

      if (hasAuthorized())
        doGet(request.body.dataParts)
      else
        Unauthorized("Token Invalid")
    }

  def extract: Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData) { implicit request =>

      if (hasAuthorized())
        doExtract(request.body.dataParts)
      else
        Unauthorized("Token Invalid")
    }

  private def doSave(data: Map[String, Seq[String]]) = try {
    val certAlias = data("cert_alias").head
    facade.store(certAlias)

    Ok(""" { "code": "0" } """)
  } catch {
    case e: IllegalStateException =>
      logger.info(e.getMessage)
      Ok(s""" { "code": "0", "message", "${e.getMessage}"} """)
    case e: NullPointerException =>
      logger.warn(e.getMessage, e)
      BadRequest
    case t: Throwable =>
      logger.error(t.getMessage, t)
      InternalServerError
  }

  private def doGet(data: Map[String, Seq[String]]) = try {
    val certAlias = data("cert_alias").head
    val cert = encoder.encodeToString(facade.get(certAlias))

    Ok(cert)
  } catch {
    case e: Exception =>
      logger.warn(e.getMessage, e)
      BadRequest
  }

  private def doExtract(data: Map[String, Seq[String]]) = try {
    val cert = data("cert").head
    val x509 = facade.extract(Base64.getDecoder.decode(cert))

    Ok(
      s"""
        |{
        | "subject_dn" : "${x509.getSubjectDN}"
        | , "issuer" : "${x509.getIssuerX500Principal.getName}"
        |}
        |""".stripMargin)
  } catch {
    case e: Exception =>
      logger.warn(e.getMessage, e)
      BadRequest
  }

}