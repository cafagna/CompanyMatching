package services

import model.Company
import model.Gram

import scala.concurrent.Future

trait CompanyMatcher {
  /**
   * match the given company profile against the legal company entities
   *
   * @return a (sorted; best match first) collection of matched legal company entities
   */
  def matchCompanies(companyProfile: Company): Future[Seq[Company]]
}

object CompanyMatcher {

  val companies: Seq[Company] = Company.load(play.Play.application().getFile(Company.entity_file))
  val CurrentStrategy: CompanyMatcher = new CompanySimilarityMatcher(companies)
}

class CompanySimilarityMatcher(entities: Seq[Company]) extends CompanyMatcher {
  private val word_grams = entities.map { c => (c, c.grams) }

  private val MAX_CANDIDATES_PER_GRAM = 200.0
  private val SIMILARITY_THRESHOLD = 0.9

  private val gram_companies = entities.flatMap { company => company.grams.map { (gram => gram -> company) } }
    .groupBy { case (gram, _) => gram }
    .filter { case (gram, _) => gram.freq >= math.log10(entities.size / MAX_CANDIDATES_PER_GRAM) + 1 }

  private val url_entities = entities.groupBy(_.website_url_clean)

  def matchCompanies(profile: Company): Future[Seq[Company]] = {
    val entitiesWithSameUrl = profile.website_url_clean match {
      case value if value.isEmpty() || value == null => Seq.empty
      case value: String => url_entities.get(value).getOrElse(Seq.empty)
    }

    if (entitiesWithSameUrl.nonEmpty) {
      //take all the companies with different names but same website as the query "cp" and find their matches on name similarity
      Future.successful(
        entitiesWithSameUrl.map { c => (c.company_name_clean, c) }.toMap
          .flatMap {
            case (name, company) => matchCompaniesOnNameSimilarity(company)
          }.toSeq.distinct)
    } else

      Future.successful(
        //use similarity on the company name to find the matching companies
        matchCompaniesOnNameSimilarity(profile))
  }

  def matchCompaniesOnNameSimilarity(profile: Company): Seq[Company] = {
    val gramsOfCandidateCompanies = gram_companies.filterKeys(profile.grams.toSet)

    val companyWithGrams = gramsOfCandidateCompanies.flatMap {
      case (gram, companiesWithGrams) =>
        companiesWithGrams.map {
          case (gram, candidateCompany) =>
            val commonGrams = candidateCompany.grams.intersect(profile.grams)
            val unionedGrams = candidateCompany.grams.union(profile.grams).diff(commonGrams)

            candidateCompany -> (commonGrams, unionedGrams)
        }
    }

    val jaccardSimilarity = companyWithGrams
      .map {
        case (company, (commonGrams, unionedGrams)) =>
          (company, commonGrams.map(_.freq).sum / unionedGrams.map(_.freq).sum)
      }

    val maxSimilarity = if (jaccardSimilarity.nonEmpty) { jaccardSimilarity.map(_._2).max } else 0.0

    jaccardSimilarity.filter({ case (cname, s) => s >= (maxSimilarity * SIMILARITY_THRESHOLD) }).map({ case (cname, s) => cname }).toSeq
  }
}
