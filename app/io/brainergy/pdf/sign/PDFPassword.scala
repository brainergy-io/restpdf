package io.brainergy.pdf.sign


import java.io.ByteArrayOutputStream

import com.itextpdf.kernel.pdf._
import javax.inject.Singleton


@Singleton
class PDFPassword {

  def apply(src: PdfReader, encryptwith: Array[Byte]): ByteArrayOutputStream = {
    val props: WriterProperties = new WriterProperties()
      .setStandardEncryption(encryptwith
        , encryptwith
        , EncryptionConstants.ALLOW_PRINTING
        , EncryptionConstants.ENCRYPTION_AES_256)

    val result = new ByteArrayOutputStream()
    val newPdf: PdfWriter = new PdfWriter(result, props)

    val pdfDoc = new PdfDocument(src, newPdf)
    pdfDoc.close()

    result
  }

}
