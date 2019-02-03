package eu.epitech.kureuil
package api

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import eu.epitech.kureuil.api.authn.AuthUtils
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto._

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
//
import backend.KureuilDatabase

case class LoginForm( email: String, password: String )
object LoginForm {
  implicit val encoder: Encoder[LoginForm] = deriveEncoder
  implicit val decoder: Decoder[LoginForm] = deriveDecoder
}

case class RegisterForm( name: String, email: String, password: String )
object RegisterForm {
  implicit val encoder: Encoder[RegisterForm] = deriveEncoder
  implicit val decoder: Decoder[RegisterForm] = deriveDecoder
}

class RegistrationRoutes( val backend: KureuilDatabase, jwtSecret: String )( implicit val ec: ExecutionContext ) {

  import akka.http.scaladsl.server.Directives._

  val login: Route =
    path( "user" / "login" ) {
      (post & entity( as[LoginForm] )) { login =>
        val user = backend.getUser( login.email )
        val resp = user.map {
          case Some( u ) =>
            if (AuthUtils.checkPassword( jwtSecret, login.password, u.password )) {
              val jwt = new AuthUtils.JwtToken( jwtSecret ).apply( login.email )
              respondWithHeader( RawHeader( "Access-Control-Expose-Headers", "Access-Token, Authorization" ) ) {
                respondWithHeader( RawHeader( "Access-Token", jwt ) ) {
                  complete( StatusCodes.OK )
                }
              }
            } else {
              complete( StatusCodes.Unauthorized )
            }
          case _ =>
            complete( StatusCodes.Unauthorized )
        }
        onComplete( resp ) {
          case Success( value ) => value
          case Failure( ex )    => complete( ( StatusCodes.InternalServerError, s"An error occured" ) )
        }
      }
    }

  def register: Route =
    path( "user" / "register" ) {
      (post & entity( as[RegisterForm] )) { register =>
        val hash = new AuthUtils.HmacSha256( jwtSecret ).apply( register.password )
        complete( backend.registerUser( register.name, register.email, hash ) )
      }
    }

  def route: Route = {
    login ~ register
  }
}

object RegistrationRoutes {}
