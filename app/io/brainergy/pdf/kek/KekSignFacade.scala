package io.brainergy.pdf.kek

import java.io._
import javax.inject.{Inject, Singleton}

@Singleton
class KekSignFacade @Inject()() extends KekSignMeta {

  def apply(form: KekSignForm): ByteArrayOutputStream = {
    val result = new ByteArrayOutputStream()
    val signer = buildSigner(form.pdfBytes, Option(form.docPassword), result)
    sign(signer =  signer
      , masterAlias = form.masterAlias
      , masterPassword = form.masterPassword
      , kekB64 = form.kekB64
      , certB64 = form.certB64
      , docClose = form.docClosed)

    result
  }

}
