package controllers

import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import services.EvaluationService

import scala.concurrent.ExecutionContext

object EvaluationController extends Controller {

  implicit val ec: ExecutionContext = Akka.system.dispatcher

  def evaluate(): Action[AnyContent] = Action.async { implicit request =>
    EvaluationService.simpleEval().map(e => Ok(views.html.eval(e)))
  }
}
