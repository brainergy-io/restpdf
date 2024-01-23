package controllers

import com.typesafe.scalalogging.LazyLogging
import io.brainergy.definitions.Ref
import io.brainergy.pdf.kek.{KekSignAppearanceFacade, KekSignAppearanceForm}
import io.brainergy.util.BJson

import javax.inject.{Inject, Singleton}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class KekSignAppearanceController @Inject()(facade: KekSignAppearanceFacade)
  extends BasedController
    with LazyLogging
    with BJson {

  def sign = Action(parse.byteString) { implicit request =>
    if (hasAuthorized())
      toOption(request.body.decodeString("UTF-8"), new Ref[KekSignAppearanceForm] {})
        .map(f => Ok(facade(f).toByteArray).as("file/bin"))
        .getOrElse(BadRequest("invalid json body"))
    else
      Unauthorized("Token Invalid")
  }

}
