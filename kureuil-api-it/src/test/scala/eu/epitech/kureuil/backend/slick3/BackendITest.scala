package eu.epitech.kureuil
package backend
package slick3

import cats.instances.future._
import cats.instances.string._
import cats.instances.vector._
import cats.syntax.apply._
import cats.syntax.option._
import cats.syntax.traverse._
import java.util.concurrent.Executors
import org.scalacheck.support.cats._
import org.scalacheck.Gen
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BackendITest extends KureuilDatabaseSpec[ITestBackend]( ITestBackend.backend ) {

  import future._
  import TestDatabase._

  override implicit val timeout: Duration = 2.minutes

  override implicit def ec: ExecutionContext = ExecutionContext.fromExecutorService( Executors.newCachedThreadPool )

  override def afterEach() = {}
  override def afterAll() = {
    b.reset().await
    super.afterAll()
  }

  val processors = Runtime.getRuntime.availableProcessors()

  "The runtime" should {
    "have at least 2 threads" in {
      assert( processors > 1 )
    }
  }

}
