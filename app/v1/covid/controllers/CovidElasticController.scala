package v1.covid.controllers

import io.swagger.annotations.{Api, ApiParam, ApiResponse, ApiResponses}
import play.api.data.Form
import play.api.i18n.MessagesProvider
import play.api.libs.json.Json
import play.api.mvc._
import v1.covid.controllers.CovidController.implicitCovidRowWrites
import v1.covid.repositories.{CovidElasticRepository, CovidRow}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
@Api(value = "Covid DataSet using Elastic Provider", produces = "application/json")
class CovidElasticController @Inject()(val controllerComponents: ControllerComponents, repo: CovidElasticRepository)
                                      (implicit ec: ExecutionContext) extends CovidController {

  def index(@ApiParam(value = "Page") page: Option[Int]): Action[AnyContent] = Action.async {
    implicit request =>
      repo.list(page).map {
        r => Ok(Json.prettyPrint(Json.toJson(r.map(f => Json.toJson(f)(implicitCovidRowWrites)))))
      }
  }

  /*
    def index: Action[AnyContent] = Action.async {
      implicit request =>
        repo.list().map {
          r => Ok(Json.toJson(r.map(f => Json.toJson(f)(implicitCovidRowWrites))))
        }
    }
     */
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

  @ApiResponses(Array(new ApiResponse(code = 404, message = "Covid Row not found")))
  def get(@ApiParam(value = "ID of the Covid data row") uid: String): Action[AnyContent] = Action.async {
    implicit request =>
      repo.get(uid).map {
        case Some(n) =>
          Ok(Json.prettyPrint(Json.toJson(n)(implicitCovidRowWrites)))
        case None =>
          NotFound
      }
  }
  /*
  def get(uid: String): Action[AnyContent] = Action.async {
    implicit request =>
      repo.get(uid).map {
        case Some(n) =>
          Ok(Json.prettyPrint(Json.toJson(n)(implicitCovidRowWrites)))
        case None =>
          NotFound
      }
  }*/


}
