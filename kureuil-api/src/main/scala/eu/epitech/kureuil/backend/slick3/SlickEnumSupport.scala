package eu.epitech.kureuil
package backend
package slick3

import enumeratum.Enum
import enumeratum.EnumEntry
import java.sql.PreparedStatement
import java.sql.ResultSet
import scala.reflect.ClassTag
import slick.ast.FieldSymbol
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcType

trait SlickEnumSupport {

  val profile: JdbcProfile

  def enumType[A <: EnumEntry]( sqlEnumTypeName: String )( implicit E: Enum[A], C: ClassTag[A] ): JdbcType[A]

}

trait PostgresEnumSupport extends SlickEnumSupport {

  import profile.DriverJdbcType

  final override def enumType[A <: EnumEntry](
      sqlEnumTypeName: String
  )( implicit E: Enum[A], C: ClassTag[A] ): JdbcType[A] =
    new DriverJdbcType[A] {
      private def toStr( v: A ) = if (v == null) null else v.entryName

      override def sqlType: Int = java.sql.Types.OTHER

      override def sqlTypeName( sym: Option[FieldSymbol] ): String = sqlEnumTypeName

      override def valueToSQLLiteral( value: A ): String =
        if (value == null) "NULL" else s"'${value.entryName}'"

      override def setValue( v: A, p: PreparedStatement, idx: Int ): Unit =
        p.setObject( idx, toStr( v ), sqlType )

      override def getValue( r: ResultSet, idx: Int ): A = {
        val value = r.getString( idx )
        if (r.wasNull()) null.asInstanceOf[A] else E.withName( value )
      }

      override def updateValue( v: A, r: ResultSet, idx: Int ): Unit =
        r.updateObject( idx, toStr( v ), sqlType )

    }

}

trait H2EnumSupport extends SlickEnumSupport {
  import profile.DriverJdbcType

  final override def enumType[A <: EnumEntry](
      sqlEnumTypeName: String
  )( implicit E: Enum[A], C: ClassTag[A] ): JdbcType[A] =
    new DriverJdbcType[A] {

      private def toInt( value: A ): Int = E.indexOf( value )

      private def fromInt( idx: Int ): A = E.values( idx )

      override def sqlType: Int = java.sql.Types.INTEGER

      override def sqlTypeName( sym: Option[FieldSymbol] ): String =
        E.values.map( v => s"'${v.entryName}'" ).mkString( "ENUM(", ", ", ")" )

      override def valueToSQLLiteral( value: A ): String =
        if (value == null) "NULL" else s"${toInt( value )}"

      override def updateValue( v: A, r: ResultSet, idx: Int ): Unit =
        if (v == null) r.updateNull( idx ) else r.updateInt( idx, toInt( v ) )

      override def getValue( r: ResultSet, idx: Int ): A = {
        val value = r.getInt( idx )
        if (r.wasNull()) null.asInstanceOf[A] else fromInt( value )
      }

      override def setValue( v: A, p: PreparedStatement, idx: Int ): Unit =
        if (v == null) p.setNull( idx, sqlType ) else p.setInt( idx, toInt( v ) )
    }

}
