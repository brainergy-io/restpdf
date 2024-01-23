package io.brainergy.pdf.sign

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.exceptions.PdfException
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf._
import com.itextpdf.signatures.PdfSignatureAppearance.RenderingMode
import com.itextpdf.signatures.PdfSigner
import com.itextpdf.signatures.PdfSigner.CryptoStandard
import io.brainergy.cert.CertFacade
import io.brainergy.util.Conf

import java.io._
import java.util.Base64
import javax.inject.{Inject, Singleton}

/**
 * ref:
 * https://kb.itextpdf.com/home/it7kb/examples/digital-signing-with-itext-7/part-iv-appearances
 */
@Singleton
class SignAppearanceFacade @Inject()(protected val certFacade: CertFacade)
  extends SignMeta {

  def apply(form: SignAppearanceForm): ByteArrayOutputStream = {
    val props = new ReaderProperties().setPassword(form.docPassword.map(_.getBytes).getOrElse(Array.empty))
    val signatures = form.signatures.sortBy(_.order)

    val pdfBytes: Array[Byte] = Base64.getDecoder.decode(form.pdfB64)

    var stream: InputStream = new ByteArrayInputStream(verifyAndRebuild(pdfBytes, props, form.hasPassword))
    var result: ByteArrayOutputStream = null
    for (i <- signatures.indices) {
      val pdfReader = new PdfReader(stream, props)
      val level = if (i == signatures.size - 1 && form.docClosed)
        PdfSigner.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS
      else
        PdfSigner.NOT_CERTIFIED

      val pdfOut = sign(pdfReader, signatures(i), level)
      result = pdfOut
      stream = new ByteArrayInputStream(pdfOut.toByteArray)
    }

    result
  }

  private def verifyAndRebuild(pdfBytes: Array[Byte], props: ReaderProperties, hasPassword: Boolean): Array[Byte] = {
    val result = new ByteArrayOutputStream()
    try {
      testFile(pdfBytes, props, result, hasPassword)
      pdfBytes
    } catch {
      case e: PdfException =>
        logger.warn(e.getMessage)
        rebuild(pdfBytes)
    }
  }

  private def sign(pdfReader: PdfReader, info: SignatureInfo, certLevel: Int): ByteArrayOutputStream = {
    val result = new ByteArrayOutputStream()
    val signer = new PdfSigner(pdfReader, result, new StampingProperties().useAppendMode())
    signer.setCertificationLevel(certLevel)

    val certBytes = certFacade.loadCert(info.certAlias)
    val cert = certFacade.buildX509(certBytes)
    val pks = RestSignature(info.keyAlias)

    val appearance = signer.getSignatureAppearance
    appearance.setPageRect(new Rectangle(info.x, info.y, info.width, info.height))
    appearance.setPageNumber(info.pageNo)
    appearance.setRenderingMode(RenderingMode.GRAPHIC)
    appearance.setSignatureGraphic(ImageDataFactory.create(D.decode(info.imgB64)))
    appearance.setCertificate(cert)

    signer.setFieldName(s"${System.currentTimeMillis}")
    signer.signDetached(DIGEST
      , pks
      , List(cert).toArray
      , CRL_LIST
      , OCSP_CLIENT
      , TSA_CLIENT
      , ESTIMATED_SIZE
      , CryptoStandard.CADES)

    result
  }

}
