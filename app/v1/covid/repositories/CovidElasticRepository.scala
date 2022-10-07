package v1.covid.repositories

import com.datastax.driver.core.querybuilder.QueryBuilder.insertInto
import com.sksamuel.elastic4s.ElasticApi.search
import play.api.MarkerContext
import v1.providers.{CassandraProvider, ElasticProvider}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.get.GetResponse
import com.sksamuel.elastic4s.{Hit, HitReader, Indexable, RequestFailure, RequestSuccess}
import play.api.libs.json.Json
import v1.covid.controllers.CovidController.implicitCovidRowWrites

import scala.util.Try

@Singleton
class CovidElasticRepository @Inject()(implicit ec: PostExecutionContext) extends CovidRepository {

  implicit object CovidRowHitReader extends HitReader[CovidRow] {
    override def read(hit: Hit): Try[CovidRow] = {
      val source = hit.sourceAsMap
      Try(CovidRow(
        source.getOrElse("cord_uidsha", "").toString, source.getOrElse("source_x", "").toString,
        source.getOrElse("title", "").toString, source.getOrElse("doi", "").toString,
        source.getOrElse("pmcid", "").toString, source.getOrElse("pubmed_id", "").toString,
        source.getOrElse("license", "").toString, source.getOrElse("abstract", "").toString,
        source.getOrElse("publish_time", "").toString, source.getOrElse("authors", "").toString,
        source.getOrElse("journal", "").toString, source.getOrElse("mag_id", "").toString,
        source.getOrElse("who_covidence_id", "").toString, source.getOrElse("arxiv_id", "").toString,
        source.getOrElse("pdf_json_files", "").toString, source.getOrElse("pmc_json_files", "").toString,
        source.getOrElse("url", "").toString, source.getOrElse("s2_id", "").toString
      ))
    }
  }

  implicit object CovidRowIndexable extends Indexable[CovidRow] {
    override def json(t: CovidRow): String = Json.toJson(t)(implicitCovidRowWrites).toString()
  }

  override def list(page: Option[Int])(implicit mc: MarkerContext): Future[Set[CovidRow]] =
    Future {
      val response = ElasticProvider.client.execute {
        search("covid").limit(page.getOrElse(1000))
      }.await
      response.result.to[CovidRow].toSet
    }

  override def create(covidRow: CovidRow)(
    implicit mc: MarkerContext): Future[Option[CovidRow]] =
    Future {
      val result = ElasticProvider.client.execute {
        indexInto("covid") doc covidRow refresh(RefreshPolicy.IMMEDIATE)
      }.await

      if (result.isSuccess)
        Some(covidRow)
      else
        None
    }

  override def get(uid: String)(
    implicit mc: MarkerContext): Future[Option[CovidRow]] =
    Future {
      val response = ElasticProvider.client.execute {
        search("covid") query uid
      }.await

      response.result.to[CovidRow].toSet.headOption
    }

}
