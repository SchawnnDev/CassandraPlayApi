package v1.covid

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesProvider}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CovidController @Inject()(val controllerComponents: ControllerComponents, repo: CovidRepositoryImpl)
                               (implicit ec: ExecutionContext)
  extends BaseController with I18nSupport {

  val implicitCovidRowWrites: Writes[CovidRow] = (covidRow: CovidRow) => {
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

  def index: Action[AnyContent] = Action.async {
    implicit request =>
      repo.list().map {
        r => Ok(Json.toJson(r.map(f => Json.toJson(f)(implicitCovidRowWrites))))
      }
  }

  def process: Action[AnyContent] = Action.async {
    implicit request => processJsonPost()
  }

  private val form: Form[CovidRow] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "cord_uidsha" -> nonEmptyText, "source_x" -> nonEmptyText,
        "title" -> nonEmptyText, "doi" -> nonEmptyText,
        "pmcid" -> nonEmptyText, "pubmed_id" -> nonEmptyText,
        "license" -> nonEmptyText, "abstract_x" -> nonEmptyText,
        "publish_time" -> nonEmptyText, "authors" -> nonEmptyText, "journal" -> nonEmptyText,
        "mag_id" -> nonEmptyText, "who_covidence_id" -> nonEmptyText,
        "arxiv_id" -> nonEmptyText, "pdf_json_files" -> nonEmptyText,
        "pmc_json_files" -> nonEmptyText, "url" -> nonEmptyText, "s2_id" -> nonEmptyText
      )(CovidRow.apply)(CovidRow.unapply)
    )
  }

  private def processJsonPost[A]()(
    implicit request: Request[A], provider: MessagesProvider): Future[Result] = {

    def failure(badForm: Form[CovidRow]) = {
      Future.successful(BadRequest)
      //Future.successful(BadRequest(badForm.errorsAsJson(provider)))
    }

    def success(input: CovidRow) = {
      repo.create(input).map {
        case Some(n) => Ok(Json.prettyPrint(Json.toJson(n)(implicitCovidRowWrites)))
        case None =>
          BadRequest
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  def get(uid: String): Action[AnyContent] = Action.async {
    implicit request =>
      repo.get(uid).map {
        case Some(n) =>
          Ok(Json.prettyPrint(Json.toJson(n)(implicitCovidRowWrites)))
        case None =>
          NotFound
      }
  }

}
