package controllers

import java.security.Security

import io.brainergy.util.{Conf, SSLCertificate}
import javax.inject.Singleton
import org.bouncycastle.jce.provider.BouncyCastleProvider
import play.api.mvc.{Action, AnyContent}

/**
 * @author Peerapat A on Feb 11, 2022
 */
@Singleton
class RootController extends BasedController
  with SSLCertificate {

  trustAllCert()
  Security.addProvider(new BouncyCastleProvider)

  private val version: String = Conf("build.version", "NA")
  private val commitId: String = Conf("build.commit.id", "NA")

  def rootOptions: Action[AnyContent] = options("/")

  def options(url: String): Action[AnyContent] = Action {
    NoContent
  }

  def buildInfo: Action[AnyContent] = Action {
    Ok(s"""{ "version":"$version", "commit_id":"$commitId" }""")
      .as(JSON)
  }

}
