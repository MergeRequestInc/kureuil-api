package eu.epitech.kureuil.api.authn
//
import io.igl.jwt._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
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

  final class JwtToken( key: String ) {
    def apply( email: String ): String = {
      val jwt = new DecodedJwt( Seq( Alg( Algorithm.HS256 ), Typ( "JWT" ) ), Seq( Iss( email ) ) )
      jwt.encodedAndSigned( key )
    }
  }

  def checkPassword( secret: String, password: String, hash: String ): Boolean = {
    new HmacSha256( secret ).apply( password ) == hash
  }
}
