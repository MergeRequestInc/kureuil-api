package eu.epitech.kureuil.schema

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import slick.basic.DatabaseConfig
import slick.codegen.SourceCodeGenerator
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import slick.model.Model
import slick.model.QualifiedName

object Main {
  def codegen( model: Model ): SourceCodeGenerator = new SourceCodeGenerator( model ) {

    override def writeToFile(
        profile: String,
        folder: String,
        pkg: String,
        container: String,
        fileName: String
    ): Unit = super.writeToFile( profile, folder, pkg, container, fileName )

    override def parentType: Option[String] = Some( "eu.epitech.kureuil.backend.slick3.SlickEnumSupport" )
    override def code: String               = super.code

    override def entityName: String => String =
      dbTableName => "Db" + dbTableName.dropRight( 1 ).toLowerCase.toCamelCase

    override def tableName: String => String = dbTableName => "Db" + super.tableName( dbTableName )

    override def Table = new Table( _ ) {

      def thisTableName: String = {
        val QualifiedName( n, _, _ ) = model.name
        n
      }

      override def EntityType: EntityType = new EntityType {
        override def caseClassFinal: Boolean = false
      }

      override def TableValue: TableValue = new TableValue {
        override def rawName: String = super.rawName.drop( 2 ).uncapitalize
      }

      override def Column = new Column( _ ) {
        override def rawType: String = ( thisTableName, model.name ) match {
          case ( _, _ ) => super.rawType
        }
      }

    }

    override def writeStringToFile( content: String, folder: String, pkg: String, fileName: String ): Unit = {
      val marker = "with Tables"
      val idx    = content.indexOf( marker ) + marker.length
      val hackedContent = {
        val ( head, tail ) = content.splitAt( idx )
        head +
          """  with eu.epitech.kureuil.backend.slick3.PostgresEnumSupport""".stripMargin +
          tail
      }

      super.writeStringToFile( hackedContent, folder, pkg, fileName )
    }

  }

  def computeModel( dc: DatabaseConfig[JdbcProfile] ): DBIO[Model] =
    for {
      m <- dc.profile.createModel( None, ignoreInvalidDefaults = false ).withPinnedSession
    } yield
      m.copy(
        tables = m.tables.filter( t => t.name.table != "schema_version" && t.name.table != "flyway_schema_history" )
      )

  def run( out: String, pkg: String ): Unit = {
    println( s"[KUREUIL-API-CODEGEN] Generating slick bindings to package $pkg in $out" )

    val dc                         = DatabaseConfig.forConfig[JdbcProfile]( "kureuil-db" )
    val slickDriver                = if (dc.profileIsObject) dc.profileName else "new " + dc.profileName
    val modelFuture: Future[Model] = dc.db.run( computeModel( dc ) )

    val outputFuture: Future[Unit] = modelFuture.flatMap { m =>
      val cg = codegen( m )
      Future( cg.writeToFile( slickDriver, out, pkg ) )
    }

    try {
      Await.result( outputFuture, Duration.Inf )
    } finally dc.db.close
  }

  def main( args: Array[String] ): Unit = args.toList match {
    case out :: pkg :: Nil => run( out, pkg )
    case _ =>
      throw new IllegalArgumentException( "Usage: [main] outDir pkg" )
  }
}
