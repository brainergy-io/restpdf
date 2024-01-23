package io.brainergy.pdf.verify

case class VerifyResult(signatureIntegrity: Boolean, hasModified: Boolean, issuerDN: String,
                        subjectDN: String, startDate: String, finalDate: String, signDate: String)

