package io.brainergy.pdf.kek

import com.fasterxml.jackson.annotation.JsonIgnore

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Base64

case class KekSignForm(private val pdfB64: String = ""
                       , private val pdfData: Array[Byte] = Array.empty
                       , docClosed: Boolean
                       , docPassword: String
                       , masterAlias: String
                       , masterPassword: String
                       , kekB64: String
                       , certB64: String) {

  @JsonIgnore
  val pdfBytes: Array[Byte] = if (pdfData.nonEmpty)
    pdfData
  else
    Base64.getDecoder.decode(pdfB64)

}
