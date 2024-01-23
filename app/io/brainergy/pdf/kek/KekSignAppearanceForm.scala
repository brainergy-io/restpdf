package io.brainergy.pdf.kek

import com.fasterxml.jackson.annotation.JsonIgnore

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Base64

case class KekSignAppearanceForm(pdfB64: String = ""
                                 , pdfPath: String = ""
                                 , docClosed: Boolean
                                 , docPassword: Option[String]
                                 , signatures: List[KekSignatureInfo]) {

}

case class KekSignatureInfo(imgB64: String
                            , order: Int
                            , pageNo: Int
                            , x: Float
                            , y: Float
                            , width: Float
                            , height: Float
                            , masterAlias: String
                            , masterPassword: String
                            , kek: String
                            , cert: String)
