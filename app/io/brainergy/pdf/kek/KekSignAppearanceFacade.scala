package io.brainergy.pdf.kek

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.exceptions.PdfException
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf._
import com.itextpdf.signatures.PdfSignatureAppearance.RenderingMode
import com.itextpdf.signatures.PdfSigner
import com.itextpdf.signatures.PdfSigner.CryptoStandard

import java.io._
import java.util.Base64
import javax.inject.{Inject, Singleton}

/**
 * ref:
 * https://kb.itextpdf.com/home/it7kb/examples/digital-signing-with-itext-7/part-iv-appearances
 */
@Singleton
class KekSignAppearanceFacade @Inject()() extends KekSignMeta {

  def apply(form: KekSignAppearanceForm): ByteArrayOutputStream = {
    val props = new ReaderProperties().setPassword(form.docPassword.map(_.getBytes).getOrElse(Array.empty))
    val signatures = form.signatures.sortBy(_.order)

    val pdfBytes: Array[Byte] = Base64.getDecoder.decode(form.pdfB64)

    var stream: InputStream = new ByteArrayInputStream(verifyAndRebuild(pdfBytes, props))
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

  private def verifyAndRebuild(pdfBytes: Array[Byte], props: ReaderProperties): Array[Byte] = {
    val result = new ByteArrayOutputStream()
    try {
      testFile(pdfBytes, props, result)
      pdfBytes
    } catch {
      case e: PdfException =>
        logger.warn(e.getMessage)
        rebuild(pdfBytes)
    }
  }

  private def sign(pdfReader: PdfReader, info: KekSignatureInfo, certLevel: Int): ByteArrayOutputStream = {
    val result = new ByteArrayOutputStream()
    val signer = new PdfSigner(pdfReader, result, new StampingProperties().useAppendMode())
    signer.setCertificationLevel(certLevel)

    val cert = buildX509(Base64.getDecoder.decode(info.cert))
    val pks = RestSignature(info.masterAlias, info.masterPassword, info.kek)

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
