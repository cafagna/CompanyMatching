package model

import scala.runtime.ScalaRunTime._ // todrop

import scala.util.Try

import java.io.File

import org.slf4j.{ Logger, LoggerFactory }
import play.api.libs.json._
//import model.Gram.{}

final case class Company(
  id: Int,
  company_name: String,
  website_url: String,
  foundation_year: Int,
  city: String,
  country: String,
  company_name_clean: String,
  website_url_clean: String,
  grams: Seq[Gram])

object Company {
  private def cleanCompanyName(str: String): String =
    {
      (str.toLowerCase().map { case '_' => ' ' case ',' => ' ' case '-' => ' ' case '+' => ' ' case '(' => ' ' case ')' => ' ' case c => c })
        .replaceAll("(e\\.k\\.)|(e\\. k\\.)|(e\\. kfr\\.)|(e\\.kfr\\.)|(e\\.kfm\\.)|(e\\. kfm\\.)", "")
        .replace(".", "")
        .replace("gesellschaft mit beschränkter haftung", "gmbh")
        .replace("mit beschränkter haftung", "gmbh")
        .replace("aktiengesellschaft", "ag")
        .replaceAll(" ag(\\s|$)", " ")
        .replaceAll(" gmbh(\\s|$)", " ")
        .replaceAll(" gesmbh(\\s|$)", " ")
        .replaceAll(" mbh(\\s|$)", " ")
        .replaceAll(" mbb(\\s|$)", " ")
        .replace(" & co. kg", " ")
        .replace(" & co.", " ")
        .trim().replaceAll(" +", " ")
    }

  def create(id: Int, company_name: String, website_url: String, foundation_year: Int, city: String, country: String): Company =
    {
      val cleanedName: String = cleanCompanyName(company_name)
      val cleanedUrl: String = website_url.toLowerCase()
        .replace("http://", "")
        .replace("https://", "")
        .replaceAll("^(www\\.)+", "")
        .replaceAll("((\\.at)|(\\.ch)|(\\.de)|(\\.com)|(\\.info)|(\\.eu))(.)*$", "")
      val grams = (Gram.tokenize(cleanedName).map { token => gramsWithFrequencies.getOrElse(token, new Gram(token, 1)) }).toSeq

      Company(id, company_name, website_url, foundation_year, city, country, cleanedName, cleanedUrl, grams)
    }

  implicit def format: Format[Company] = Json.format[Company]

  val entity_file = "company_entities.csv"
  val profile_file = "company_profiles.csv"

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  val gramsWithFrequencies = Gram.getFrequencies(io.Source.fromFile(entity_file).getLines.map { line =>
    cleanCompanyName(
      {
        val attributeValues = line.split("\t")

        attributeValues.size match {
          case n if n < 2 => throw new Exception("Expect at least two attributes in file " + entity_file + ", found " + n + "instead")
          case _: Int => attributeValues(1)
        }
      })
  }.toSeq)

  def load(f: File): Seq[Company] = {
    println("reading " + f.getName)

    io.Source.fromFile(f).getLines.map { line =>
      {
        val cols = line.split("\t")
        cols.size match {
          case n if n < 2 => throw new Exception("Expect at least two attributes in file " + entity_file + ", found " + n + "instead")
          case _: Int => Company.create(cols(0).toInt, cols(1), Try(cols(2)).getOrElse(null), Try(cols(3).toInt).getOrElse(-1),
            Try(cols(4)).getOrElse(null), Try(cols(5)).getOrElse(null))
        }

      }
    }.toSeq
  }
}
