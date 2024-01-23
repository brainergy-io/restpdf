package controllers

import com.typesafe.scalalogging.LazyLogging
import io.brainergy.pdf.verify.VerifyFacade
import io.brainergy.util.BJson
import play.api.mvc.{Action, AnyContent}

import java.io.ByteArrayInputStream
import java.util.Base64
import javax.inject.{Inject, Singleton}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class VerifyController @Inject()(facade: VerifyFacade)
  extends BasedController
    with LazyLogging
    with BJson {

  private lazy val D = Base64.getDecoder

  def verify: Action[AnyContent] = Action { implicit request =>
    request.body.asText
      .map(b64 => facade.processStrem(new ByteArrayInputStream(D.decode(b64.trim))))
      .map(r => Ok(toJsonString(r)))
      .getOrElse(BadRequest("Missing Data"))
  }

}
