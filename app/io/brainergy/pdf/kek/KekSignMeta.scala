package io.brainergy.pdf.kek

import com.itextpdf.kernel.exceptions.PdfException
import com.itextpdf.kernel.pdf._
import com.itextpdf.signatures.PdfSigner.CryptoStandard
import com.itextpdf.signatures._
import com.typesafe.scalalogging.LazyLogging
import io.brainergy.pdf.sign.X509
import io.brainergy.util.Conf

import java.io._
import java.security.cert.{CertificateFactory, X509Certificate}
import java.util
import java.util.Base64

/**
 * <p>
 * NOT_CERTIFIED— creates an ordinary signature aka an approval or a recipient signature. A document can be signed for approval by one or more recipients.
 *
 * CERTIFIED_NO_CHANGES_ALLOWED— creates a certification signature aka an author signature. After the signature is applied, no changes to the document will be allowed.
 *
 * CERTIFIED_FORM_FILLING— creates a certification signature for the author of the document. Other people can still fill out form fields or add approval signatures without invalidating the signature.
 *
 * CERTIFIED_FORM_FILLING_AND_ANNOTATIONS— creates a certification signature. Other people can still fill out form fields- or add approval signatures as well as annotations without invalidating the signature.
 */
trait KekSignMeta extends LazyLogging
  with X509 {

  protected def sign(signer: PdfSigner
                     , masterAlias: String
                     , masterPassword: String
                     , kekB64: String
                     , certB64: String
                     , docClose: Boolean): Unit = {
    val cert = buildX509(Base64.getDecoder.decode(certB64))

    val certs = List(cert)

    val pks = RestSignature(masterAlias, masterPassword, kekB64)
    if (docClose)
      signer.setCertificationLevel(PdfSigner.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS)
    else
      signer.setCertificationLevel(PdfSigner.NOT_CERTIFIED)

    signer.setFieldName(s"${System.currentTimeMillis}")
    signer.signDetached(DIGEST
      , pks
      , certs.toArray
      , CRL_LIST
      , OCSP_CLIENT
      , TSA_CLIENT
      , ESTIMATED_SIZE
      , CryptoStandard.CADES)
  }

  protected def buildSigner(pdfData: Array[Byte], optionPass: Option[String], result: ByteArrayOutputStream) = {
    val props = optionPass.map(p => new ReaderProperties().setPassword(p.getBytes))
      .getOrElse(new ReaderProperties())

    val signer = try {
      toPdfSigner(pdfData, props, result)
    } catch {
      case e: PdfException =>
        logger.warn(e.getMessage)
        toPdfSigner(rebuild(pdfData), props, result)
    }

    signer
  }

  protected def buildX509(certData: Array[Byte]): X509Certificate = CertificateFactory
    .getInstance("X509")
    .generateCertificate(new ByteArrayInputStream(certData))
    .asInstanceOf[X509Certificate]

  protected def testFile(bytes: Array[Byte], props: ReaderProperties, result: ByteArrayOutputStream): Unit = {
    toPdfSigner(bytes, props, result)
  }

  protected def toPdfSigner(bytes: Array[Byte], props: ReaderProperties, result: ByteArrayOutputStream): PdfSigner = {
    val pdf = new PdfReader(new ByteArrayInputStream(bytes), props)
    new PdfSigner(pdf, result, new StampingProperties().useAppendMode())
  }

  protected def rebuild(pdfData: Array[Byte]): Array[Byte] = {
    val source = new PdfReader(new ByteArrayInputStream(pdfData))
    val result = new ByteArrayOutputStream()
    val writer = new PdfWriter(result)

    val original: PdfDocument = new PdfDocument(source, writer)
    original.close()

    result.toByteArray
  }

  protected lazy val D: Base64.Decoder = Base64.getDecoder
  protected lazy val CRL_LIST: util.Collection[ICrlClient] = null
  protected lazy val OCSP_CLIENT: IOcspClient = null
  protected lazy val TSA_CLIENT: ITSAClient = null
  protected lazy val ESTIMATED_SIZE = 0
  protected lazy val DIGEST = new BouncyCastleDigest()
  protected lazy val HSM_URL: String = Conf("hsm.url")
  protected lazy val HSM_TOKEN: String = Conf("hsm.token")

}
