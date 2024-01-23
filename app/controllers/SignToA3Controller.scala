package controllers

import com.typesafe.scalalogging.LazyLogging
import io.brainergy.pdf.sign.SignToA3Facade
import io.brainergy.util.BJson
import javax.inject.{Inject, Singleton}
import play.api.libs.Files
import play.api.mvc.{Action, MultipartFormData}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class SignToA3Controller @Inject()(signer: SignToA3Facade)
  extends BasedController
    with LazyLogging
    with BJson {

  def sign: Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData) { implicit request =>
      if (hasAuthorized())
        process(request.body.dataParts)
      else
        Unauthorized("Token Invalid")
    }

  private def process(data: Map[String, Seq[String]]) = try {
    val pdfB64 = data("pdf_file").head
    val xmlB64 = data("xml_file").head
    val keyAlias = data("key_alias").head
    val certAlias = data("cert_alias").head
    val encryptBy = if (data.contains("encrypt_by")) Some(data("encrypt_by").head) else None
    val result = signer(pdfB64, xmlB64, keyAlias, certAlias, encryptBy)

    Ok(result.toByteArray).as("file/bin")
  } catch {
    case _: NullPointerException =>
      BadRequest
    case t: Throwable =>
      logger.warn(t.getMessage, t)
      InternalServerError
  }

}
