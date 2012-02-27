import org.scalatra._
import java.net.URL
import scalate.ScalateSupport

import net.liftweb.mongodb._
import net.liftweb.json._
import net.liftweb.mongodb.record.MongoRecord
import javax.servlet._

import com.github.jasonjackson.User 
import com.github.jasonjackson.EmailActor

import org.slf4j.{LoggerFactory}

class MyScalatraServlet extends ScalatraServlet 
  with AuthenticationSupport
  with FlashMapSupport
  with ScalateSupport 
  {

  override def initialize(config: ServletConfig): Unit = {
    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("127.0.0.1", 27017), "users"))
    val logger = LoggerFactory.getLogger(getClass)    
    super.initialize(config)
  }


  before() {
    if (!isAuthenticated) {
      scentry.authenticate('RememberMe)
    }
  }

  get("/") {
    templateEngine.layout("/WEB-INF/views/index.scaml", Map("content" -> "Login or Register"))
  }

  get("/login") {
    redirectIfAuthenticated

    templateEngine.layout("/WEB-INF/views/login_form.scaml", Map("error" -> flash.getOrElse("error", "")))
  }

  post("/login") {
    val u : String = params("userEmail")
    val p : String = params("password")

    // Add validation, something like this maybe?:  https://gist.github.com/985761
    if (User.isValidated(u, p)) {

      scentry.authenticate('UserPassword)

      if (isAuthenticated) {
        redirect("/loggedin")
      }else{
        flash += ("error" -> "login failed")
        redirect("/login")
      }

    }else{
      flash += ("error" -> "Your account isn't validated yet")
      redirect("/revalidate")
    }
  }

  get("/revalidate") {
    contentType = "text/html"

    templateEngine.layout("/WEB-INF/views/revalidate.scaml", Map("heading" -> "Email validation required", "content" -> "Must validate your account before signing in.  Check your inbox."))
  }

  get("/loggedin") {
    redirectIfNotAuthenticated
    contentType = "text/html"
    templateEngine.layout("/WEB-INF/views/loggedin.scaml", Map("user" -> user))
  }

  get("/register") {
    contentType = "text/html"
    templateEngine.layout("/WEB-INF/views/registration_form.scaml", Map("error" -> flash.getOrElse("error", "")))
  }

  post("/register") {
    val u = User.createRecord
      .username(params("userName"))
      .email(params("userEmail"))
      .password(params("password"))
      .validated(false)
      .validation_code(md5hash(params("userEmail")))

    u.save

    val mail_package = Map[String,String]("email" -> u.email.toString, "validation_code" -> u.validation_code.toString)

    // Send the validation email to the actor, should get this out of the servlet but its good enough for a quicky example...
    val actor = new EmailActor
    actor.start
    actor ! mail_package
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

  get("/validate/:validation_code") {
    User.validateUser(params("validation_code")) match {
      case None => {
        flash += ("info" -> "Validation code not found") 
        redirect("/login")
      }
      case Some(usr) => {
        flash += ("info" -> "Account validated!")
        redirect("/login")
      }
    }
  }

  notFound {
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound() 
  }

  //logger.info("User getClass: " + user.getClass.getSimpleName)
  //logger.info("User manOf: " + manOf(user))
  def manOf[T: Manifest](t: T): Manifest[T] = manifest[T]

  def md5hash(s: String) = {
    val m = java.security.MessageDigest.getInstance("MD5")
    val b = s.getBytes("UTF-8")
    m.update(b, 0, b.length)
    new java.math.BigInteger(1, m.digest()).toString(16)
  }
}
