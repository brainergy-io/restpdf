package io.brainergy.pdf.verify

import java.io.InputStream
import com.itextpdf.kernel.pdf.{PdfDocument, PdfReader, ReaderProperties}
import com.itextpdf.signatures.SignatureUtil

import java.text.SimpleDateFormat
import javax.inject.Singleton

@Singleton
class VerifyFacade {

  def apply(filename: String, password: String = null): Array[VerifyResult] = {
    process(loadFile(filename, password))
  }

  def processStrem(file: InputStream, password: String = null): Array[VerifyResult] = {
    process(loadStream(file, password))
  }

  def process(doc: PdfDocument): Array[VerifyResult] = {
    val signatureUtil = new SignatureUtil(doc)

    val sigs = signatureUtil.getSignatureNames
    try {
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      sigs.toArray.map(signature => {
        val sigTail = signatureUtil.readSignatureData(signature.toString, "BC")
        VerifyResult(signatureIntegrity = sigTail.verifySignatureIntegrityAndAuthenticity
          , hasModified = !signatureUtil.signatureCoversWholeDocument(signature.toString)
          , issuerDN = sigTail.getSigningCertificate.getIssuerDN.getName
          , subjectDN = sigTail.getSigningCertificate.getSubjectDN.getName
          , startDate = dateFormat.format(sigTail.getSigningCertificate.getNotBefore)
          , finalDate = dateFormat.format(sigTail.getSigningCertificate.getNotAfter)
          , signDate = dateFormat.format(sigTail.getSignDate.getTime)
        )
      })
    } finally {
      doc.close()
    }
  }

  private def loadFile(filename: String, password: String = null): PdfDocument = if (password == null || password.isEmpty)
    new PdfDocument(new PdfReader(filename))
  else
    new PdfDocument(new PdfReader(filename, new ReaderProperties().setPassword(password.getBytes)))

  private def loadStream(file: InputStream, password: String = null): PdfDocument = if (password == null || password.isEmpty)
    new PdfDocument(new PdfReader(file))
  else
    new PdfDocument(new PdfReader(file, new ReaderProperties().setPassword(password.getBytes)))

}
