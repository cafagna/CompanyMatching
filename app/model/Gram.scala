package model

import scala.runtime.ScalaRunTime._ // todrop

import scala.util.Try

import java.io.File

import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._



case class Gram(key: String, freq: Double)


object Gram {
  val GRAM_SIZE = 4 
  
  implicit def format: Format[Gram] = Json.format[Gram]
  
  def tokenize(value: String) = value.split(" ").flatMap{_.sliding(GRAM_SIZE)}
  
  def getFrequencies(companyNames: Seq[String]): Map[String, Gram] = {
    val grams= companyNames.map{case (cname) => (cname, tokenize(cname).toSet)}.flatMap{case (cname, grams) =>(grams)}.toSeq
     grams.groupBy(x=>x).mapValues{ companies =>         math.log10(companyNames.size/companies.size.toDouble) + 1}
    .map{case(gr, freq) => (gr, Gram(gr, freq)) }
  }
}
