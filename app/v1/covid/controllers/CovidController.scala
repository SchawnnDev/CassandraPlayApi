package v1.covid.controllers

import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, Writes}
import play.api.mvc.BaseController
import v1.covid.repositories.CovidRow

object CovidController {
  lazy val implicitCovidRowWrites: Writes[CovidRow] = (covidRow: CovidRow) => {
    Json.obj(
      "cord_uidsha" -> covidRow.cord_uidsha, "source_x" -> covidRow.source_x,
      "title" -> covidRow.title, "doi" -> covidRow.doi, "pmcid" -> covidRow.pmcid, "pubmed_id" -> covidRow.pubmed_id,
      "license" -> covidRow.license, "abstract" -> covidRow.abstract_x, "publish_time" -> covidRow.publish_time,
      "authors" -> covidRow.authors, "journal" -> covidRow.journal, "mag_id" -> covidRow.mag_id,
      "who_covidence_id" -> covidRow.who_covidence_id, "arxiv_id" -> covidRow.arxiv_id,
      "pdf_json_files" -> covidRow.pdf_json_files, "pmc_json_files" -> covidRow.pmc_json_files,
      "url" -> covidRow.url, "s2_id" -> covidRow.s2_id
    )
  }
}

abstract class CovidController extends BaseController with I18nSupport {

}
