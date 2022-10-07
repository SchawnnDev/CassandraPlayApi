package v1.covid.repositories

import akka.actor.ActorSystem
import com.datastax.driver.core.querybuilder.QueryBuilder.insertInto
import com.datastax.driver.core.{Row, SimpleStatement}
import play.api.MarkerContext
import play.api.libs.concurrent.CustomExecutionContext
import utils.CassandraPaging
import v1.providers.CassandraProvider

import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.Future

object CovidRow {
  def fromRow(r: Row): CovidRow = CovidRow(
    r.getString("cord_uidsha"), r.getString("source_x"), r.getString("title"),
    r.getString("doi"), r.getString("pmcid"), r.getString("pubmed_id"),
    r.getString("license"), r.getString("abstract"), r.getString("publish_time"),
    r.getString("authors"), r.getString("journal"), r.getString("mag_id"),
    r.getString("who_covidence_id"), r.getString("arxiv_id"), r.getString("pdf_json_files"),
    r.getString("pmc_json_files"), r.getString("url"), r.getString("s2_id")
  )

}

// 18 vars
case class CovidRow(cord_uidsha: String, source_x: String, title: String, doi: String, pmcid: String, pubmed_id: String,
                    license: String, abstract_x: String, publish_time: String, authors: String, journal: String,
                    mag_id: String, who_covidence_id: String, arxiv_id: String, pdf_json_files: String,
                    pmc_json_files: String, url: String, s2_id: String)

trait CovidRepository {
  def create(covidRow: CovidRow)(implicit mc: MarkerContext): Future[Option[CovidRow]]

  // def update() = ???
  // def deleteByUid() = ???
  def get(uid: String)(implicit mc: MarkerContext): Future[Option[CovidRow]]

  def list(page: Option[Int])(implicit mc: MarkerContext): Future[Set[CovidRow]]
}

class PostExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")
