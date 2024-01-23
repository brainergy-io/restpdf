package io.brainergy.cert


import io.brainergy.pdf.sign.RestCertificate
import io.brainergy.util.Conf

import java.io._
import java.nio.file.{Files, Paths}
import java.security.cert.{CertificateFactory, X509Certificate}
import javax.inject.{Inject, Singleton}

@Singleton
class CertFacade @Inject()(val restCert: RestCertificate) {

  def store(certAlias: String): Unit = {
    val fileName = buildPath(certAlias)
    if (Files.exists(Paths.get(fileName)))
      throw new IllegalStateException("Cert Exist")

    val certBin = restCert.fetch(certAlias)
    try {
      buildX509(certBin)
    } catch {
      case _: Exception => throw new IllegalStateException("Cert Error")
    }

    Files.write(Paths.get(fileName), certBin)
  }

  def get(certAlias: String): Array[Byte] = loadCert(certAlias)

  def extract(cert: Array[Byte]): X509Certificate = {
    buildX509(cert)
  }

  def loadCert(certAlias: String): Array[Byte] = {
    val fileName = buildPath(certAlias)
    if (Files.exists(Paths.get(fileName)))
      Files.readAllBytes(Paths.get(buildPath(certAlias)))
    else
      restCert.fetch(certAlias)
  }

  def buildX509(certData: Array[Byte]): X509Certificate = CertificateFactory
    .getInstance("X509")
    .generateCertificate(new ByteArrayInputStream(certData))
    .asInstanceOf[X509Certificate]

  private def buildPath(certAlias: String) = s"$CERTS_DIRECTORY/$certAlias"

  private lazy val CERTS_DIRECTORY = Conf("certs.directory", "/app/resources")

}
