package eu.epitech.kureuil.backend.slick3
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.PostgresProfile
} with Tables with eu.epitech.kureuil.backend.slick3.PostgresEnumSupport

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables extends eu.epitech.kureuil.backend.slick3.SlickEnumSupport {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(
    apiTokens.schema,
    channels.schema,
    links.schema,
    linkTags.schema,
    tags.schema,
    userChannels.schema,
    users.schema
  ).reduceLeft( _ ++ _ )
  @deprecated( "Use .schema instead of .ddl", "3.0" )
  def ddl = schema

  /** Entity class storing rows of table apiTokens
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param uid Database column uid SqlType(uuid)
    *  @param token Database column token SqlType(bpchar), Length(64,false)
    *  @param read Database column read SqlType(bool), Default(false)
    *  @param write Database column write SqlType(bool), Default(false)
    *  @param admin Database column admin SqlType(bool), Default(false)
    *  @param comment Database column comment SqlType(text), Default(None) */
  case class DbApiToken(
      id: Long,
      uid: java.util.UUID,
      token: String,
      read: Boolean = false,
      write: Boolean = false,
      admin: Boolean = false,
      comment: Option[String] = None
  )

  /** GetResult implicit for fetching DbApiToken objects using plain SQL queries */
  implicit def GetResultDbApiToken(
      implicit e0: GR[Long],
      e1: GR[java.util.UUID],
      e2: GR[String],
      e3: GR[Boolean],
      e4: GR[Option[String]]
  ): GR[DbApiToken] = GR { prs =>
    import prs._
    DbApiToken.tupled(
      ( <<[Long], <<[java.util.UUID], <<[String], <<[Boolean], <<[Boolean], <<[Boolean], <<?[String] )
    )
  }

  /** Table description of table api_tokens. Objects of this class serve as prototypes for rows in queries. */
  class DbApiTokens( _tableTag: Tag ) extends profile.api.Table[DbApiToken]( _tableTag, "api_tokens" ) {
    def * = ( id, uid, token, read, write, admin, comment ) <> (DbApiToken.tupled, DbApiToken.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (
        Rep.Some( id ),
        Rep.Some( uid ),
        Rep.Some( token ),
        Rep.Some( read ),
        Rep.Some( write ),
        Rep.Some( admin ),
        comment
      ).shaped.<>(
        { r =>
          import r._; _1.map( _ => DbApiToken.tupled( ( _1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7 ) ) )
        },
        (_: Any) => throw new Exception( "Inserting into ? projection not supported." )
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]( "id", O.AutoInc, O.PrimaryKey )

    /** Database column uid SqlType(uuid) */
    val uid: Rep[java.util.UUID] = column[java.util.UUID]( "uid" )

    /** Database column token SqlType(bpchar), Length(64,false) */
    val token: Rep[String] = column[String]( "token", O.Length( 64, varying = false ) )

    /** Database column read SqlType(bool), Default(false) */
    val read: Rep[Boolean] = column[Boolean]( "read", O.Default( false ) )

    /** Database column write SqlType(bool), Default(false) */
    val write: Rep[Boolean] = column[Boolean]( "write", O.Default( false ) )

    /** Database column admin SqlType(bool), Default(false) */
    val admin: Rep[Boolean] = column[Boolean]( "admin", O.Default( false ) )

    /** Database column comment SqlType(text), Default(None) */
    val comment: Rep[Option[String]] = column[Option[String]]( "comment", O.Default( None ) )

    /** Uniqueness Index over (uid) (database name api_tokens_uid_key) */
    val index1 = index( "api_tokens_uid_key", uid, unique = true )
  }

  /** Collection-like TableQuery object for table apiTokens */
  lazy val apiTokens = new TableQuery( tag => new DbApiTokens( tag ) )

  /** Entity class storing rows of table channels
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param name Database column name SqlType(varchar), Length(255,true)
    *  @param query Database column query SqlType(text) */
  case class DbChannel( id: Long, name: String, query: String )

  /** GetResult implicit for fetching DbChannel objects using plain SQL queries */
  implicit def GetResultDbChannel( implicit e0: GR[Long], e1: GR[String] ): GR[DbChannel] = GR { prs =>
    import prs._
    DbChannel.tupled( ( <<[Long], <<[String], <<[String] ) )
  }

  /** Table description of table channels. Objects of this class serve as prototypes for rows in queries. */
  class DbChannels( _tableTag: Tag ) extends profile.api.Table[DbChannel]( _tableTag, "channels" ) {
    def * = ( id, name, query ) <> (DbChannel.tupled, DbChannel.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      ( Rep.Some( id ), Rep.Some( name ), Rep.Some( query ) ).shaped.<>( { r =>
        import r._; _1.map( _ => DbChannel.tupled( ( _1.get, _2.get, _3.get ) ) )
      }, (_: Any) => throw new Exception( "Inserting into ? projection not supported." ) )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]( "id", O.AutoInc, O.PrimaryKey )

    /** Database column name SqlType(varchar), Length(255,true) */
    val name: Rep[String] = column[String]( "name", O.Length( 255, varying = true ) )

    /** Database column query SqlType(text) */
    val query: Rep[String] = column[String]( "query" )

    /** Uniqueness Index over (name,query) (database name channels_name_query_key) */
    val index1 = index( "channels_name_query_key", ( name, query ), unique = true )
  }

  /** Collection-like TableQuery object for table channels */
  lazy val channels = new TableQuery( tag => new DbChannels( tag ) )

  /** Entity class storing rows of table links
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param url Database column url SqlType(varchar), Length(2083,true), Default(None) */
  case class DbLink( id: Long, url: Option[String] = None )

  /** GetResult implicit for fetching DbLink objects using plain SQL queries */
  implicit def GetResultDbLink( implicit e0: GR[Long], e1: GR[Option[String]] ): GR[DbLink] = GR { prs =>
    import prs._
    DbLink.tupled( ( <<[Long], <<?[String] ) )
  }

  /** Table description of table links. Objects of this class serve as prototypes for rows in queries. */
  class DbLinks( _tableTag: Tag ) extends profile.api.Table[DbLink]( _tableTag, "links" ) {
    def * = ( id, url ) <> (DbLink.tupled, DbLink.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      ( Rep.Some( id ), url ).shaped.<>( { r =>
        import r._; _1.map( _ => DbLink.tupled( ( _1.get, _2 ) ) )
      }, (_: Any) => throw new Exception( "Inserting into ? projection not supported." ) )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]( "id", O.AutoInc, O.PrimaryKey )

    /** Database column url SqlType(varchar), Length(2083,true), Default(None) */
    val url: Rep[Option[String]] = column[Option[String]]( "url", O.Length( 2083, varying = true ), O.Default( None ) )

    /** Uniqueness Index over (url) (database name links_url_key) */
    val index1 = index( "links_url_key", url, unique = true )
  }

  /** Collection-like TableQuery object for table links */
  lazy val links = new TableQuery( tag => new DbLinks( tag ) )

  /** Entity class storing rows of table linkTags
    *  @param idLink Database column id_link SqlType(int8)
    *  @param idTag Database column id_tag SqlType(int8) */
  case class DbLinkTag( idLink: Long, idTag: Long )

  /** GetResult implicit for fetching DbLinkTag objects using plain SQL queries */
  implicit def GetResultDbLinkTag( implicit e0: GR[Long] ): GR[DbLinkTag] = GR { prs =>
    import prs._
    DbLinkTag.tupled( ( <<[Long], <<[Long] ) )
  }

  /** Table description of table link_tags. Objects of this class serve as prototypes for rows in queries. */
  class DbLinkTags( _tableTag: Tag ) extends profile.api.Table[DbLinkTag]( _tableTag, "link_tags" ) {
    def * = ( idLink, idTag ) <> (DbLinkTag.tupled, DbLinkTag.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      ( Rep.Some( idLink ), Rep.Some( idTag ) ).shaped.<>( { r =>
        import r._; _1.map( _ => DbLinkTag.tupled( ( _1.get, _2.get ) ) )
      }, (_: Any) => throw new Exception( "Inserting into ? projection not supported." ) )

    /** Database column id_link SqlType(int8) */
    val idLink: Rep[Long] = column[Long]( "id_link" )

    /** Database column id_tag SqlType(int8) */
    val idTag: Rep[Long] = column[Long]( "id_tag" )

    /** Primary key of linkTags (database name link_tags_pkey) */
    val pk = primaryKey( "link_tags_pkey", ( idLink, idTag ) )

    /** Foreign key referencing links (database name link_tags_id_link_fkey) */
    lazy val dbLinksFk = foreignKey( "link_tags_id_link_fkey", idLink, links )(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )

    /** Foreign key referencing tags (database name link_tags_id_tag_fkey) */
    lazy val dbTagsFk = foreignKey( "link_tags_id_tag_fkey", idTag, tags )(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )
  }

  /** Collection-like TableQuery object for table linkTags */
  lazy val linkTags = new TableQuery( tag => new DbLinkTags( tag ) )

  /** Entity class storing rows of table tags
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param name Database column name SqlType(varchar), Length(255,true), Default(None) */
  case class DbTag( id: Long, name: Option[String] = None )

  /** GetResult implicit for fetching DbTag objects using plain SQL queries */
  implicit def GetResultDbTag( implicit e0: GR[Long], e1: GR[Option[String]] ): GR[DbTag] = GR { prs =>
    import prs._
    DbTag.tupled( ( <<[Long], <<?[String] ) )
  }

  /** Table description of table tags. Objects of this class serve as prototypes for rows in queries. */
  class DbTags( _tableTag: Tag ) extends profile.api.Table[DbTag]( _tableTag, "tags" ) {
    def * = ( id, name ) <> (DbTag.tupled, DbTag.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      ( Rep.Some( id ), name ).shaped.<>( { r =>
        import r._; _1.map( _ => DbTag.tupled( ( _1.get, _2 ) ) )
      }, (_: Any) => throw new Exception( "Inserting into ? projection not supported." ) )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]( "id", O.AutoInc, O.PrimaryKey )

    /** Database column name SqlType(varchar), Length(255,true), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]( "name", O.Length( 255, varying = true ), O.Default( None ) )

    /** Uniqueness Index over (name) (database name tags_name_key) */
    val index1 = index( "tags_name_key", name, unique = true )
  }

  /** Collection-like TableQuery object for table tags */
  lazy val tags = new TableQuery( tag => new DbTags( tag ) )

  /** Entity class storing rows of table userChannels
    *  @param idUser Database column id_user SqlType(int8)
    *  @param idChannel Database column id_channel SqlType(int8)
    *  @param isAdmin Database column is_admin SqlType(bool), Default(false)
    *  @param isSubscribed Database column is_subscribed SqlType(bool), Default(false) */
  case class DbUserChannel( idUser: Long, idChannel: Long, isAdmin: Boolean = false, isSubscribed: Boolean = false )

  /** GetResult implicit for fetching DbUserChannel objects using plain SQL queries */
  implicit def GetResultDbUserChannel( implicit e0: GR[Long], e1: GR[Boolean] ): GR[DbUserChannel] = GR { prs =>
    import prs._
    DbUserChannel.tupled( ( <<[Long], <<[Long], <<[Boolean], <<[Boolean] ) )
  }

  /** Table description of table user_channels. Objects of this class serve as prototypes for rows in queries. */
  class DbUserChannels( _tableTag: Tag ) extends profile.api.Table[DbUserChannel]( _tableTag, "user_channels" ) {
    def * = ( idUser, idChannel, isAdmin, isSubscribed ) <> (DbUserChannel.tupled, DbUserChannel.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      ( Rep.Some( idUser ), Rep.Some( idChannel ), Rep.Some( isAdmin ), Rep.Some( isSubscribed ) ).shaped.<>(
        { r =>
          import r._; _1.map( _ => DbUserChannel.tupled( ( _1.get, _2.get, _3.get, _4.get ) ) )
        },
        (_: Any) => throw new Exception( "Inserting into ? projection not supported." )
      )

    /** Database column id_user SqlType(int8) */
    val idUser: Rep[Long] = column[Long]( "id_user" )

    /** Database column id_channel SqlType(int8) */
    val idChannel: Rep[Long] = column[Long]( "id_channel" )

    /** Database column is_admin SqlType(bool), Default(false) */
    val isAdmin: Rep[Boolean] = column[Boolean]( "is_admin", O.Default( false ) )

    /** Database column is_subscribed SqlType(bool), Default(false) */
    val isSubscribed: Rep[Boolean] = column[Boolean]( "is_subscribed", O.Default( false ) )

    /** Primary key of userChannels (database name user_channels_pkey) */
    val pk = primaryKey( "user_channels_pkey", ( idUser, idChannel ) )

    /** Foreign key referencing channels (database name user_channels_id_channel_fkey) */
    lazy val dbChannelsFk = foreignKey( "user_channels_id_channel_fkey", idChannel, channels )(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )

    /** Foreign key referencing users (database name user_channels_id_user_fkey) */
    lazy val dbUsersFk = foreignKey( "user_channels_id_user_fkey", idUser, users )(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )
  }

  /** Collection-like TableQuery object for table userChannels */
  lazy val userChannels = new TableQuery( tag => new DbUserChannels( tag ) )

  /** Entity class storing rows of table users
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param name Database column name SqlType(varchar), Length(255,true)
    *  @param email Database column email SqlType(varchar), Length(255,true)
    *  @param password Database column password SqlType(varchar), Length(256,true)
    *  @param admin Database column admin SqlType(bool), Default(false) */
  case class DbUser( id: Long, name: String, email: String, password: String, admin: Boolean = false )

  /** GetResult implicit for fetching DbUser objects using plain SQL queries */
  implicit def GetResultDbUser( implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean] ): GR[DbUser] = GR { prs =>
    import prs._
    DbUser.tupled( ( <<[Long], <<[String], <<[String], <<[String], <<[Boolean] ) )
  }

  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class DbUsers( _tableTag: Tag ) extends profile.api.Table[DbUser]( _tableTag, "users" ) {
    def * = ( id, name, email, password, admin ) <> (DbUser.tupled, DbUser.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      ( Rep.Some( id ), Rep.Some( name ), Rep.Some( email ), Rep.Some( password ), Rep.Some( admin ) ).shaped.<>(
        { r =>
          import r._; _1.map( _ => DbUser.tupled( ( _1.get, _2.get, _3.get, _4.get, _5.get ) ) )
        },
        (_: Any) => throw new Exception( "Inserting into ? projection not supported." )
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]( "id", O.AutoInc, O.PrimaryKey )

    /** Database column name SqlType(varchar), Length(255,true) */
    val name: Rep[String] = column[String]( "name", O.Length( 255, varying = true ) )

    /** Database column email SqlType(varchar), Length(255,true) */
    val email: Rep[String] = column[String]( "email", O.Length( 255, varying = true ) )

    /** Database column password SqlType(varchar), Length(256,true) */
    val password: Rep[String] = column[String]( "password", O.Length( 256, varying = true ) )

    /** Database column admin SqlType(bool), Default(false) */
    val admin: Rep[Boolean] = column[Boolean]( "admin", O.Default( false ) )

    /** Uniqueness Index over (name,email) (database name users_name_email_key) */
    val index1 = index( "users_name_email_key", ( name, email ), unique = true )
  }

  /** Collection-like TableQuery object for table users */
  lazy val users = new TableQuery( tag => new DbUsers( tag ) )
}
