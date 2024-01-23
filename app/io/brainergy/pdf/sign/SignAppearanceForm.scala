package io.brainergy.pdf.sign

case class SignAppearanceForm(pdfB64: String = ""
                              , pdfPath: String = ""
                              , docClosed: Boolean
                              , docPassword: Option[String]
                              , signatures: List[SignatureInfo]) {
  def hasPassword: Boolean = docPassword.isDefined && docPassword.get.nonEmpty
}

case class SignatureInfo(imgB64: String
                         , order: Int
                         , pageNo: Int
                         , x: Float
                         , y: Float
                         , width: Float
                         , height: Float
                         , keyAlias: String
                         , certAlias: String)
