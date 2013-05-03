package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.{TaskDAO, Task}

object Application extends Controller {

  val taskForm = Form {
    "label" -> nonEmptyText
  }

  def index = Action {
    Redirect(routes.Application.tasks)
  }

  def tasks = Action {
    Ok(views.html.index(TaskDAO.all(), taskForm))
  }

  def newTask = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(TaskDAO.all(), errors)),
      label => {
        TaskDAO.createTask(label)
        Redirect(routes.Application.tasks())
  }
    )
  }

  def deleteTask(id: Int) = Action {
    TaskDAO.deleteTask(id)
    Redirect(routes.Application.tasks())
  }
}