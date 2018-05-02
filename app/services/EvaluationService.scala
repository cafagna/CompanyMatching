package services

import java.io.File

import model.Company
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

import scala.io.Source //

object EvaluationService {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)
 

    val profiles2: Seq[Company] = Company.load(play.Play.application().getFile(Company.profile_file))

  val groundTruth: Map[Int, Set[Int]] = loadGroundTruth(play.Play.application().getFile("ground_truth.csv"))

  /**
    * load the ground truth from file.
    * @param f the file from which to load the ground truth. it is supposed to be a csv file (whitespace separated)
    * @return a map containing the ground truth (profile id -> (possibly multiple) entity ids)
    
  def loadGroundTruth(f: File): Map[Int, Set[Int]] = { // TODO implement
    Map(
      25680 -> Set.empty,
      34776 -> Set.empty, 
      54472 -> Set(300000807)
    )
  }*/

    def loadGroundTruth(f: File): Map[Int, Set[Int]] = { // TODO implement
    Source.fromFile(f)
    .getLines()
    .map{line => 
      val pairs = line.split("\t").map{word => word.toInt}
      val key = pairs.headOption match {
        case None => throw new Exception("")
        case Some(value) => value
      }
      
      val matches = pairs.last
      
      (key, matches)
    }.toSeq
    .groupBy{case (key, _) => key}
    .map{case (key, groups) => 
      key -> groups.map{case (_, value) => value}.toSet  
    }
  }

  def simpleEval()(implicit ec: ExecutionContext): Future[EvalResults] = {
    val evalFut: Seq[Future[(Boolean, Boolean, Boolean,  Boolean)]] = profiles2 map { cp =>
      CompanyMatcher.CurrentStrategy.matchCompanies(cp) map { matches: Seq[Company] =>
        val correct: Set[Int]    = groundTruth.getOrElse(cp.id, Set.empty)
        val idsMatched: Seq[Int] = matches.map(_.id)
        val atLeastOneCorrect    = idsMatched.exists(id => correct.contains(id))
        val atLeastOneWrong      = !idsMatched.forall(id => correct.contains(id))
        val bestMatchCorrect     = correct.intersect(idsMatched.headOption.toSet).nonEmpty

        (atLeastOneCorrect, atLeastOneWrong, bestMatchCorrect, idsMatched.isEmpty)
      }
    }
    Future.sequence(evalFut) map { eval: Seq[(Boolean, Boolean, Boolean,  Boolean)] =>
      val n = groundTruth.keys.size.toDouble
      EvalResults(
        fractionCorrect          = eval.count(_._1).toDouble / n,
        fractionWrong            = eval.count(_._2).toDouble / n,
        fractionBestMatchCorrect = eval.count(_._3).toDouble / n,
        fractionNoMatch          = eval.count(_._4).toDouble / n)
    }
  }
}

final case class EvalResults(fractionCorrect: Double, fractionWrong: Double, fractionBestMatchCorrect: Double, fractionNoMatch: Double)
