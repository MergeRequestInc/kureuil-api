package eu.epitech.kureuil

case class KureuilApiError( message: String, causes: List[Throwable] = Nil ) extends Throwable {
  final override def fillInStackTrace(): Throwable = this
}
