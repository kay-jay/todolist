package models

import play.api.db.slick.Config.driver.simple._
import org.joda.time.{DateTimeZone, DateTime}
import java.sql.Timestamp
import scala.slick.lifted.BaseTypeMapper
import play.api.db.slick.DB._
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

  implicit val joda2Timestamp = new MappedTypeMapper[DateTime, Timestamp] with BaseTypeMapper[DateTime] {
    def map(t: DateTime): Timestamp = new Timestamp(t.getMillis)

    def comap(u: Timestamp): DateTime = new DateTime(u.getTime, DateTimeZone.UTC)
  }


  def all(): List[Task] = {
    def all = for (c <- Tasks)
    yield (c.id, c.label, c.created)

     withSession { implicit session =>
      all.list().collect {
        case (id, label, created) => Task(Some(id), label, created)
      }
    }
  }

  def createTask(label: String) = {
    withSession { implicit session =>
      Tasks.forInsert.insert(Task(None, label, DateTime.now(DateTimeZone.UTC)))
    }
  }

  def deleteTask(id: Int) = {
    withSession { implicit session =>
      def getById = Query(Tasks).filter(_.id === id)
      getById.delete
    }
  }
}