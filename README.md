Matching company entities with company profiles
=======================

For a professional social network we need to match data from two different tables that provide information about companies:

- `company_entities`: lists the legal company entities
- `company_profiles`: lists the company profiles on JobsSocialNetwork

Both tables have the following structure:

| Column             | Description |
|:-------------------|:------------|
| `id`               | ID of the company entity or company profile. Careful: the entity IDs are different from the profile IDs, e.g. ID 4 in `company_entities` may refer to _Ericcson_ while ID 4 in `company_profiles` may refer to _Siemens_ |
| `company_name`     | name of the company |
| `website_url`      | URL that may link to the company website |
| `foundation_year`  | year when the company was founded |
| `city`             | name of the city where the company is located |
| `country`          | code of the country where the company is located |

We assume that there is a 1:n relationship between company profiles and company entities, i.e. one company profile may
refer to zero or more company entities. The [ground truth](ground_truth.csv) (company profile id, company entity id) is available.

**Task:** 
Design and implement a lightweight algorithm that matches company entities and company profiles: for each company profile it must return the list of legal company entities that the company profile may refer to.


## Running the service

- the application can be built and run with [sbt](http://www.scala-sbt.org/)
- The following classes were already provided and had not been written by me:
  + `controllers.EvaluationController`: the evaluation controller 
  + `controllers.MatchingController`: the controller for `/api/match` 
- when you start the application via `sbt run` then you can  go to [http://localhost:9000](http://localhost:9000) to see some stats about the [current strategy](app/services/CompanyMatcher.scala) that is used for matching company profiles and company entities
