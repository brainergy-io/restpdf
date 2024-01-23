package io.brainergy.pdf.sign

import com.itextpdf.kernel.pdf._
import com.itextpdf.kernel.pdf.filespec.PdfFileSpec
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.pdfa.PdfADocument
import com.itextpdf.signatures.PdfSigner
import com.typesafe.scalalogging.LazyLogging
import io.brainergy.cert.CertFacade
import io.brainergy.util.Conf

import java.io._
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class SignToA3Facade @Inject()(protected val certFacade: CertFacade
                               , protected val passwordFacade: PDFPassword) extends SignMeta
  with LazyLogging {

  def apply(pdfB64: String
            , xmlB64: String
            , keyAlias: String
            , certAlias: String
            , encryptBy: Option[String]): ByteArrayOutputStream = {

    val (pdfA3, props) = if (encryptBy.isDefined) {
      (passwordFacade(new PdfReader(new ByteArrayInputStream(toA3(pdfB64, xmlB64).toByteArray)), encryptBy.get.getBytes)
        , new ReaderProperties().setPassword(encryptBy.get.getBytes))
    } else {
      (toA3(pdfB64, xmlB64), new ReaderProperties())
    }

    val result = new ByteArrayOutputStream()
    val pdf = new PdfReader(new ByteArrayInputStream(pdfA3.toByteArray), props)
    val signer = new PdfSigner(pdf, result, new StampingProperties())

    sign(signer
      , keyAlias
      , certAlias
      , docClose = true)

    result
  }

  private def toA3(pdfB64: String, xmlB64: String): ByteArrayOutputStream = {
    val source = new PdfReader(new ByteArrayInputStream(D.decode(pdfB64)))
    val result = new ByteArrayOutputStream()
    val original: PdfDocument = new PdfDocument(source)
    val newPDF = new PdfWriter(result)
    val pdfa = new PdfADocument(newPDF
      , PdfAConformanceLevel.PDF_A_3U
      , newPdfOutputIntent())

    try {
      val embeddedFileName = "INV.xml"
      val embeddedFileDescription = "ETAX Invoice"
      val embeddedFileContentBytes = D.decode(xmlB64)
      val spec = PdfFileSpec.createEmbeddedFileSpec(pdfa
        , embeddedFileContentBytes
        , embeddedFileDescription
        , embeddedFileName
        , null
        , null
        , null)

      // This method adds file attachment at document level.
      pdfa.addFileAttachment("ETAX_", spec)
      new PdfMerger(pdfa).merge(original, 1, original.getNumberOfPages)

      result
    } catch {
      case t: Throwable => logger.error(t.getMessage, t)
        throw t
    } finally {
      Try(original.close())
      Try(pdfa.close())
    }
  }

  private def newPdfOutputIntent(): PdfOutputIntent = new PdfOutputIntent("Custom"
    , ""
    , "http://www.color.org"
    , "sRGB IEC61966-2.1"
    , new FileInputStream(CS_PROFILE))

  private lazy val CS_PROFILE = Conf("rgbprofile.path", "resources/sRGB_CS_profile.icm")

}
