package controllers

import com.typesafe.scalalogging.LazyLogging
import io.brainergy.pdf.sign.{SignFacade, SignForm}
import io.brainergy.util.{BJson, Conf}
import play.api.libs.Files
import play.api.mvc.{Action, MultipartFormData}

import javax.inject.{Inject, Singleton}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class SignController @Inject()(signer: SignFacade)
  extends BasedController
    with LazyLogging
    with BJson {

  def sign: Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData) { implicit request =>

      if (hasAuthorized())
        process(request.body.dataParts, Array.empty)
      else
        Unauthorized("Token Invalid")
    }

  private def process(data: Map[String, Seq[String]], pdfBytes: Array[Byte]) = try {
    val pdfB64 = if (data.contains("pdf_file")) data("pdf_file").head else ""
    val docClose = data("doc_close").head.toBoolean
    val docPassword = if (data("doc_password").head == "") null else data("doc_password").head
    val keyAlias = data("key_alias").head
    val certAlias = data("cert_alias").head

    val result = signer(SignForm(pdfB64, pdfBytes, docClose, docPassword, keyAlias, certAlias))

    Ok(result.toByteArray).as("file/bin")
  } catch {
    case _: NullPointerException =>

      BadRequest
    case t: Throwable =>
      logger.warn(t.getMessage, t)
      InternalServerError
  }

}
