package controllers

import com.typesafe.scalalogging.LazyLogging
import io.brainergy.pdf.kek.{KekSignFacade, KekSignForm}
import io.brainergy.util.BJson
import play.api.libs.Files
import play.api.mvc.{Action, MultipartFormData}

import javax.inject.{Inject, Singleton}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class KeKSignController @Inject()(signer: KekSignFacade)
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
    val pdfB64 = if (data.contains("pdf_b64")) data("pdf_b64").head else ""
    val docClose = data("doc_close").head.toBoolean
    val docPassword = data("doc_password").head
    val masterAlias = data("master_alias").head
    val masterPassword = data("master_password").head
    val kek = data("kek").head
    val cert = data("cert").head

    val result = signer(KekSignForm(pdfB64, pdfBytes, docClose, docPassword, masterAlias, masterPassword, kek, cert))

    Ok(result.toByteArray).as("file/bin")
  } catch {
    case _: NullPointerException =>
      BadRequest
    case t: Throwable =>
      logger.warn(t.getMessage, t)
      InternalServerError
  }

}