package io.brainergy.util

import java.util.Base64

trait B64 {

  protected val decoder = Base64.getDecoder
  protected val encoder = Base64.getEncoder

}
