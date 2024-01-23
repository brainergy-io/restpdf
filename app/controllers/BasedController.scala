package controllers

import io.brainergy.util.Conf
import play.api.mvc.{BaseController, ControllerComponents, Request}

import javax.inject.Inject

/**
 * @author Peerapat A on Feb 11, 2022
 */
abstract class BasedController extends BaseController {

  @Inject
  private var cc: ControllerComponents = _

  override def controllerComponents: ControllerComponents = cc

  def hasAuthorized[T]()(implicit request: Request[T]): Boolean = !unAuthorized()

  def unAuthorized[T]()(implicit request: Request[T]): Boolean = !request.headers.get("X-Access-Token").contains(TOKEN)

  private lazy val TOKEN = Conf("hsm.access")

}
