package controllers

import model.Company
import org.slf4j.{Logger, LoggerFactory}
import services.CompanyMatcher
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current

import scala.concurrent.ExecutionContext

object MatchingController extends Controller {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  implicit val ec: ExecutionContext = Akka.system.dispatcher

  def matchCompany: Action[Company] = Action.async(parse.json[Company]) { implicit request =>
    CompanyMatcher.CurrentStrategy.matchCompanies(request.body).map(c => Ok(Json.obj("total_matches" -> c.size, "matches" -> c)))
  }
}
