package v1.covid.repositories

import com.datastax.driver.core.SimpleStatement
import com.datastax.driver.core.querybuilder.QueryBuilder.insertInto
import play.api.MarkerContext
import utils.CassandraPaging
import v1.providers.CassandraProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

@Singleton
class CovidCassandraRepository @Inject()(implicit ec: PostExecutionContext) extends CovidRepository {

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