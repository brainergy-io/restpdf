package controllers

import com.typesafe.scalalogging.LazyLogging
import io.brainergy.pdf.verify.VerifyFacade
import io.brainergy.util.BJson
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class VerifyFileController @Inject()(facade: VerifyFacade)
  extends BasedController
    with LazyLogging
    with BJson {

  def verifyHTML: Action[AnyContent] = Action {
    Ok(
      """
        |<form method="POST" action="/pdf/verify" enctype="multipart/form-data">
        |  <input type="file" id="pdffile" name="pdffile">
        |  <input type="submit">
        |</form>
        |""".stripMargin).as(HTML)
  }

  def verifyFile = Action(parse.multipartFormData) { implicit request =>
    request.body
      .file("pdf_file")
      .map(tmpFile => try {
        val file = tmpFile.ref.toFile
        val result = toJsonString(facade(file.getAbsolutePath))
        logger.info(s"Process file -> ${file.getName}")
        logger.info(s" >> $result")
        Ok(result)
      } finally {
        tmpFile.ref.delete()
      }).getOrElse(BadRequest("Missing File"))
  }

}
