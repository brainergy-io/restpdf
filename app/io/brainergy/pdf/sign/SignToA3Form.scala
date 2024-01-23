package io.brainergy.pdf.sign

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Base64

import com.fasterxml.jackson.annotation.JsonIgnore

/**
{
    "pdffile_b64": "... ",
    "doc_closed": true,
    "doc_password": "",
    "signature": [
      {
          "position_x": "334.5548549810845"
          , "position_y": "874.9772727272727"
          , "page_number":"1"
          , "dimension_width": "134.5701904296875"
          , "dimension_height": "78.038330078125"
          , "cert_alias": "XXXX_CERT"
          , "key_alias": "XXXX_KEY"
          , "img": "..."
      }
    ]
}
*/
case class SignToA3Form(pdfB64: String
                        , docClosed: Boolean
                        , docPassword: String
                        , keyAlias: String
                        , certAlias: String) {

  @JsonIgnore
  val pdfStream: InputStream = new ByteArrayInputStream(Base64.getDecoder.decode(pdfB64))

}
