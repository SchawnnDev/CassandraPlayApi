package v1.covid

import akka.actor.ActorSystem
import com.datastax.driver.core
import com.datastax.driver.core.{Row, SimpleStatement}
import com.datastax.driver.core.querybuilder.{Clause, QueryBuilder}
import com.datastax.driver.core.querybuilder.QueryBuilder.{insertInto, select}
import play.api.MarkerContext
import play.api.libs.concurrent.CustomExecutionContext
import v1.CassandraProvider

import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.Future
import utils.CassandraPaging

import java.sql.Statement

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

@Singleton
class CovidRepositoryImpl @Inject()(implicit ec: PostExecutionContext)
  extends CovidRepository {

  override def list(page: Option[Int])(implicit mc: MarkerContext): Future[Set[CovidRow]] =
    Future {
      val paging = new CassandraPaging(CassandraProvider.session)

      // paging
      page match {
        case Some(n) =>
          val limit = 20
          val start = limit * n
          val statement = new SimpleStatement("SELECT * FROM covid.dataset")
          val rows = paging.fetchRowsWithPage(statement, Math.max(1, n), limit)
          rows.asScala.map(r => CovidRow.fromRow(r)).toSet
        case _ =>
          val resultSet = CassandraProvider.session.execute("SELECT * FROM covid.dataset LIMIT 60")
          resultSet.all().asScala.map(r => CovidRow.fromRow(r)).toSet
      }

    }

  override def create(covidRow: CovidRow)(
    implicit mc: MarkerContext): Future[Option[CovidRow]] =
    Future {
      val insert = insertInto("covid", "dataset")
      insert.value("cord_uidsha", covidRow.cord_uidsha)
        .value("source_x", covidRow.source_x)
        .value("title", covidRow.title)
        .value("doi", covidRow.doi)
        .value("pmcid", covidRow.pmcid)
        .value("pubmed_id", covidRow.pubmed_id)
        .value("license", covidRow.license)
        .value("abstract", covidRow.abstract_x)
        .value("publish_time", covidRow.publish_time)
        .value("authors", covidRow.authors)
        .value("journal", covidRow.journal)
        .value("mag_id", covidRow.mag_id)
        .value("who_covidence_id", covidRow.who_covidence_id)
        .value("arxiv_id", covidRow.arxiv_id)
        .value("pdf_json_files", covidRow.pdf_json_files)
        .value("pmc_json_files", covidRow.pmc_json_files)
        .value("url", covidRow.url)
        .value("s2_id", covidRow.s2_id)
        .ifNotExists()
      val resultSet = CassandraProvider.session.execute(insert)

      if (resultSet.wasApplied())
        Some(CovidRow.fromRow(resultSet.one()))
      else
        None
    }

  override def get(uid: String)(
    implicit mc: MarkerContext): Future[Option[CovidRow]] =
    Future {
      val prepared = CassandraProvider.session.prepare("SELECT * FROM covid.dataset WHERE cord_uidsha=?")
      val resultSet = CassandraProvider.session.execute(prepared.bind(uid))
      val result = resultSet.one()

      if (result != null)
        Some(CovidRow.fromRow(result))
      else
        None
    }


}
