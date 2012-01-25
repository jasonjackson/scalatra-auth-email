import org.scalatra._
import java.net.URL
import scalate.ScalateSupport

import net.liftweb.mongodb._
import net.liftweb.json._
import net.liftweb.mongodb.record.MongoRecord
import javax.servlet._

import org.slf4j.{LoggerFactory}

class MyScalatraServlet extends ScalatraServlet 
  with AuthenticationSupport
  with FlashMapSupport
  with ScalateSupport 
  {

  override def initialize(config: ServletConfig): Unit = {
    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("127.0.0.1", 27017), "scalatra-auth"))
val logger = LoggerFactory.getLogger(getClass)    
    super.initialize(config)
  }


  before() {
    if (!isAuthenticated) {
      scentry.authenticate('RememberMe)
    }
  }

  get("/") {
    templateEngine.layout("/WEB-INF/views/index.scaml", Map("content" -> "Hello World from get"))
  }

  get("/login") {
    redirectIfAuthenticated

    templateEngine.layout("/WEB-INF/views/login_form.scaml", Map("error" -> flash.getOrElse("error", "")))
  }

  post("/login") {
    scentry.authenticate('UserPassword)

    // Add validation:  https://gist.github.com/985761

    if (isAuthenticated) {
      redirect("/loggedin")
    }else{
      flash += ("error" -> "login failed")
      redirect("/login")
    }
  }

  get("/loggedin") {
    redirectIfNotAuthenticated

    contentType = "text/html"
logger.info("TYPE O USER: " + user.getClass.getSimpleName)
logger.info("TYPE MAN OF: " + manOf(user))
    //templateEngine.layout("/WEB-INF/views/loggedin.scaml", Map("user" -> user))
    layoutTemplate("loggedin.scaml", ("layout" -> "/WEB-INF/layouts/default.scaml"), ("user" -> user))
  }

  get("/register") {
    contentType = "text/html"

    templateEngine.layout("/WEB-INF/views/registration_form.scaml", Map("error" -> flash.getOrElse("error", "")))
  }

  post("/register") {
    val u = User.createRecord
      .username(params("userName"))
      .password(params("password"))

    //save
    u.save

    redirect("/login")
  }

  get("/logout/?") {
    logOut

    redirect("/logout_step")
  }

  // this step is used to verify the cookies are erased
  get("/logout_step/?") {
    redirect("/login")
  }






//  notFound {
//          response.sendError(404)
//  }



  //protected def contextPath = request.getContextPath

  notFound {
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound() 
  }



def manOf[T: Manifest](t: T): Manifest[T] = manifest[T]




}
