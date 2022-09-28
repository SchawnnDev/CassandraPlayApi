package v1.covid

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.{Inject, Singleton}

class CovidRouter @Inject()(controller: CovidController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") => controller.index
    case POST(p"/") => controller.process
    case GET(p"/$uid") => controller.get(uid)
  }
}
