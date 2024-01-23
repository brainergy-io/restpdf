package io.brainergy.pdf.sign

import io.brainergy.cert.CertFacade

import java.io._
import javax.inject.{Inject, Singleton}

@Singleton
class SignFacade @Inject()(val certFacade: CertFacade) extends SignMeta {

  def apply(form: SignForm): ByteArrayOutputStream = {
    val result = new ByteArrayOutputStream()
    val signer = buildSigner(form.pdfBytes, Option(form.docPassword), result)
    sign(signer =  signer
      , keyAlias = form.keyAlias
      , certAlias = form.certAlias
      , docClose = form.docClosed)

    result
  }

}
