import mill._
import $ivy.`com.lihaoyi::mill-contrib-playlib:`,  mill.playlib._

object coviddataset extends PlayModule with SingleModule {

  def scalaVersion = "2.12.7"
  def playVersion = "2.7.9"
  def twirlVersion = "1.5.1"

  object test extends PlayTests
}
