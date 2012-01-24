import org.scalatra._
import java.net.URL
import scalate.ScalateSupport

class MyScalatraServlet extends ScalatraServlet with ScalateSupport {

//  before() {
//    contentType = "text/html"
//  }

  get("/") {
    templateEngine.layout("/WEB-INF/views/index.scaml", Map("content" -> "Hello World from get"))
  }

  //protected def contextPath = request.getContextPath

  notFound {
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound() 
  }
}
