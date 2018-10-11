package eu.epitech.kureuil
package api
package logs

import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset

case class HexDump( width: Int, lines: Seq[HexDumpLine] ) {

  def printTo( s: PrintWriter ): Unit = {
    lines.zipWithIndex.foreach {
      case ( l, ix ) => s.println( l.disp( width, ix ) )
    }
  }

  override def toString: String = {
    val w = new StringWriter
    printTo( new PrintWriter( w ) )
    w.toString
  }
}

object HexDump {
  val Ascii: Charset = Charset.forName( "ASCII" )
  val Utf8: Charset  = Charset.forName( "UTF-8" )
  val AsciiDot: Byte = 46

  def apply( width: Int, s: String ): HexDump = {
    val lines = s.getBytes( Utf8 ).grouped( width ).map( new HexDumpLine( _ ) )
    HexDump( width, lines.toList )
  }

  def apply( width: Int, bs: Array[Byte] ): HexDump = {
    val lines = bs.grouped( width ).map( new HexDumpLine( _ ) )
    HexDump( width, lines.toList )
  }
}

class HexDumpLine( val bytes: Array[Byte] ) {
  import HexDump._

  def printable( b: Byte ): Byte =
    if (b < 32 || b == 127) AsciiDot else b

  def disp( width: Int, ix: Int ): String = {
    val num = "%04X".format( width * ix )
    val hex = bytes.map( b => "%02X".format( b ) ).mkString( " " )
    val pad = hex.padTo( 3 * width - 1, ' ' )
    val txt = new String( bytes.map( printable ), Ascii )

    s"$num |$pad|$txt|"
  }
}
