package com.tonnoz.errorhandling

import arrow.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull

/*** In this variation, we make use of Either from Arrow core library to handle errors
 ***/
object ConsultantMatchingV5_Either {

  /***
   ***  we introduce a sealed class hierarchy [MatchingError] to represent the different types of errors that can occur
   ***/
  sealed interface MatchingError
  data class NoMatchingAssignment(val consultant: Consultant): MatchingError
  data class GenericError(val cause:String, val details: Throwable): MatchingError


  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

  class AssignmentsDao {
    fun findBestMatchingAssignment(consultant: Consultant): Assignment? =
      ASSIGNMENTS_DB.filter { assignment ->
        assignment.stack.any { skill -> consultant.skills.contains(skill) }
      }.maxByOrNull { assignment ->
        assignment.stack.intersect(consultant.skills).size
      }

  }
  class RemoteCheckerClient{
    fun clientAllowRemote(clientName: String): Boolean = runBlocking {
      val rand = (0..2).random()
      if(rand == 0) throw IOException("Service down!")
      delay(1500)
      clientName == "Aviation client"
    }
  }

  class MatchingService(
    private val assignmentsDao: AssignmentsDao = AssignmentsDao(),
    private val remoteCheckerClient: RemoteCheckerClient = RemoteCheckerClient()
  ) {

    /**
     * We wrap the return value in a [Either.right] in case the assignment is found
     * and wrap a [NoMatchingAssignment] in a [Either.left] in case the assignment is not found
     */
    fun findBestMatchingClient(consultant: Consultant): Either<MatchingError, String> =
      assignmentsDao.findBestMatchingAssignment(consultant)?.clientName?.right()
        ?: NoMatchingAssignment(consultant).left()

    /**
     * We can use the [right] and [left] extension functions from [Either] to map our success and failure cases.
     * Then we can make use of the client name (a string) with [flatMap] and call [RemoteCheckerClient.clientAllowRemote]
     * with it. But since [RemoteCheckerClient.clientAllowRemote] can potentially throw an exception, we need to
     * wrap it in a [Either.catch] as well. Finally, we map the exception to a [GenericError] (left) and return the Either.
     */
    fun remoteClientExistForConsultant(consultant: Consultant): Either<MatchingError, Boolean> =
      assignmentsDao.findBestMatchingAssignment(consultant)?.clientName?.right()
        ?.flatMap { a ->
        Either
          .catch { remoteCheckerClient.clientAllowRemote(a) }
          .mapLeft { GenericError("Remote service down", it) }
      } ?: NoMatchingAssignment(consultant).left()

    /**
     * In this variation we make use of the [either] builder scope as a monadic comprehension:
     * In this scope we get access to functions such as [ensureNotNull], [ensure] and [recover].
     * These gives us the ability to raise an error and have a little bit of flow typing (notice that we don't need
     * the "assignment.?" anymore after calling the ensureNotNull function).
     * The code is now more linear and readable too.
     */
    fun remoteClientExistForConsultantScoped(consultant: Consultant): Either<MatchingError, Boolean> = either {
      val assignment = assignmentsDao.findBestMatchingAssignment(consultant)
      ensureNotNull(assignment) { NoMatchingAssignment(consultant) } //will return the error if assignment is null
      Either.catch {  remoteCheckerClient.clientAllowRemote(assignment.clientName) }
        .mapLeft { GenericError("Remote service down", it) }.bind()
    }

  }


  @JvmStatic
  fun main(args: Array<String>) {
    val matchingService = MatchingService()
    val c1 = Consultant("Uncle Bob", setOf("c++"))
    val c2 = Consultant("Tony Hoare", setOf("java","spring"))

    val myEither = matchingService.remoteClientExistForConsultantScoped(c2)
    .onLeft {
      when(it) {
        is GenericError -> println("[${it.cause}]: ${it.details}")
        is NoMatchingAssignment -> println("No client match the skills of the candidate: $it")
      }
    }.onRight { println("there is at least one client that allow remote work for consultant ${c2.name}")}

    when(myEither){ //we can pattern match on myEither because Either is a sealed class
      is Either.Left -> println("Ouch: ${myEither.value}")
      is Either.Right -> println("Yey: ${myEither.value}")
    }

    val resultGetOrElse = myEither.getOrElse { false }

  }

}
