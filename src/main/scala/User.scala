package com.github.jasonjackson {

  import java.util.regex.Pattern
  import net.liftweb.mongodb.record._
  import net.liftweb.mongodb.record.field._
  import net.liftweb.record.field._
  import net.liftweb.record._
  import org.bson.types._
  import org.joda.time.{DateTime, DateTimeZone}
  import net.liftweb.mongodb.record.MongoRecord
  import net.liftweb.json.DefaultFormats
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonAST.JObject
  import java.security.SecureRandom
  import java.util.Date
  
  import org.slf4j.{LoggerFactory}

  class User extends MongoRecord[User] with MongoId[User] {
    def meta = User
  
    object username extends StringField(this, 200)
    object password extends StringField(this, 200)
    object email extends StringField(this, 200)
    object rememberMe extends StringField(this, 200)
    object validated extends BooleanField(this)
    object validation_code extends StringField(this, 50)
  
    def userIdAsString: String = id.toString
  
    val logger = LoggerFactory.getLogger(getClass)

    def login(u: String, p: String): Option[User] = {
      val usr = User.findAll(("email" -> u) ~ ("password" -> p))
      if(usr.length > 0){
        val tmp = Some(usr.head)
        updateRememberMe(tmp)
        // return
        tmp
      }else{
        None
      }
    }

    def validateUser(s: String): Option[User] = {
      val usr = User.findAll(("validation_code" -> s)) 
      if(usr.length > 0){
        User.update(("validation_code" -> s), ("$set" -> ("validated" -> true)))
        val tmp = Some(usr.head)
        tmp
      }else{
        None
      }
    }
 
    def isValidated(u: String, p: String) : Boolean = {
      val usr = User.findAll(("email" -> u) ~ ("password" -> p) ~ ("validated" -> true))
      if (usr.head.validated.toString == "true"){
        return true
      }else{
        return false
      }
    }

    def updateRememberMe(u: Option[User]) {
      val dt = new Date()
      u match {
          case None => None
          case Some(usr) => if(usr.rememberMe.value.isEmpty){ 
            User.update( ("_id" -> usr.id.toString),("$set"->("rememberMe" -> generateToken(dt.toString + usr.id.toString) ) ) ) 
          }
      }
    }
  
    def forgetMe(){
      val dt = new Date()
      User.update( ("_id" -> this.id.toString),("$set"->("rememberMe" -> generateToken(dt.toString + this.id.toString) ) ) ) 
    }
  
    def validateRememberToken(t: String): Option[User] = {
      // TODO: Add IP and hash into this
      var usr = User.findAll(("rememberMe" -> t))
      Some(usr.head)
    }
    
    def generateToken(s: String): String = {
      val random = SecureRandom.getInstance("SHA1PRNG")
      val str = new Array[Byte](16)
      random.nextBytes(str)
      //return
      str.toString
    }
  }
    
  object User extends User with MongoMetaRecord[User] {
  }

}
