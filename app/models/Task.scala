package models

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import org.joda.time.{DateTimeZone, DateTime}
import java.sql.Timestamp
import scala.slick.lifted.BaseTypeMapper
import play.api.db.DB
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: kayjay
 * Date: 5/1/13
 * Time: 11:05 PM
 * To change this template use File | Settings | File Templates.
 */

case class Task(id: Option[Int], label: String, created: DateTime)


object TaskDAO {
  implicit val joda2Timestamp = new MappedTypeMapper[DateTime, Timestamp] with BaseTypeMapper[DateTime] {
    def map(t: DateTime): Timestamp = new Timestamp(t.getMillis)

    def comap(u: Timestamp): DateTime = new DateTime(u.getTime, DateTimeZone.UTC)
  }

  object Tasks extends Table[Task]("tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def label = column[String]("label")

    def created = column[DateTime]("created")

    def * = id.? ~ label ~ created <>(Task, Task.unapply _)

    def forInsert = label ~ created <>( {
      t => Task(None, t._1, t._2)
    }, {
      t: Task => Some((t.label, t.created))
    })
  }

  def all(): List[Task] = {
    def all = for (c <- Tasks)
    yield (c.id, c.label, c.created)

    Database.forDataSource(DB.getDataSource()) withSession {
      all.list().collect {
        case (id, label, created) => Task(Some(id), label, created)
      }
    }
  }

  def createTask(label: String) = {
    Database.forDataSource(DB.getDataSource()) withSession {
      Tasks.forInsert.insert(Task(None, label, DateTime.now(DateTimeZone.UTC)))
    }
  }

  def deleteTask(id: Int) = {
    Database.forDataSource(DB.getDataSource()) withSession {
      def getById = Query(Tasks).filter(_.id === id)
      getById.delete
    }
  }
}