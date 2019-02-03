package eu.epitech.kureuil.api.authn
//
import io.igl.jwt._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex

import scala.util.Try
//
object AuthUtils {
  final class HmacSha256( key: String ) {
    private val algo    = "HmacSHA256"
    private val keySpec = new SecretKeySpec( key.getBytes, algo )

    def apply( arg: String ): String = {
      val mac = Mac.getInstance( algo )
      mac.init( keySpec )
      mac synchronized {
        Hex.encodeHexString( mac.doFinal( arg.getBytes() ) )
      }
    }
  }

  val algorithm               = Algorithm.HS256
  val requiredHeaders         = Set[HeaderField]( Typ )
  val requiredClaims          = Set[ClaimField]( Sub )
  val headers                 = Seq[HeaderValue]( Typ( "JWT" ), Alg( algorithm ) )
  def claims( claim: String ) = Seq[ClaimValue]( Sub( claim ) )

  final class JwtToken( key: String ) {
    def apply( email: String ): String = {
      val jwt = new DecodedJwt( headers, claims( email ) )
      jwt.encodedAndSigned( key )
    }

    def validate( token: String ): Try[Jwt] = {
      DecodedJwt.validateEncodedJwt( token, key, algorithm, requiredHeaders, requiredClaims )
    }
  }

  def checkPassword( secret: String, password: String, hash: String ): Boolean = {
    new HmacSha256( secret ).apply( password ) == hash
  }
}
