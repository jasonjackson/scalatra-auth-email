package com.github.jasonjackson {

  import org.apache.commons.mail._ 
  import scala.actors.Actor
  import Actor._
  
  class EmailActor extends Actor { 
    def act = loop {
      react {
        case m: Map[String,String] => sender(m: Map[String,String])
      }
    }

    def sender(m:Map[String,String]) { 
      val mail = new SimpleEmail();
      mail.setHostName("localhost");
      mail.setSmtpPort(25476);
      mail.setFrom("email-auth@example.com");
      mail.setSubject("Varification Email");
      mail.setMsg("To validate: localhost:8080/validate/"+m("validation_code"));
      mail.addTo(m("email").toString);
      mail.send();
    }

  }
}

// Could use HtmlEmail() and do something like:
// mail.setHtmlMsg("<html><body><b>Click to validate: <a href=\"localhost:8080/validate/"+m("validation_code")+"\">Validate!</a></b></body></html>");
