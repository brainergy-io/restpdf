package io.brainergy.pdf.sign

import com.fasterxml.jackson.annotation.JsonIgnore

import java.util.Base64

case class SignForm(private val pdfB64: String = ""
                    , private val pdfData: Array[Byte] = Array.empty
                    , docClosed: Boolean
                    , docPassword: String
                    , keyAlias: String
                    , certAlias: String) {

  @JsonIgnore
  val pdfBytes: Array[Byte] = if (pdfData.nonEmpty)
    pdfData
  else
    Base64.getDecoder.decode(pdfB64)

}
