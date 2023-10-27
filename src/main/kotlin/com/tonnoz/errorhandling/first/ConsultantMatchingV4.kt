package com.tonnoz.errorhandling.first

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.NoSuchElementException

/*** In this variation, we revert to map logical failure with Exceptions
 ** in [AssignmentsDao.findBestMatchingAssignment] but we use the
 ** [Result] class as a wrapper, applying a more functional approach in [MatchingService] and in [main]
 *  **/
object ConsultantMatchingV4 {

  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

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
     * We wrap the return value in a [Result] and use [runCatching] to automatically
     * create a [Result.failure] in case an exception is raised by [AssignmentsDao.findBestMatchingAssignment]
     */
    fun findBestMatchingClient(consultant: Consultant): Result<String> = runCatching {
      assignmentsDao.findBestMatchingAssignment(consultant)
    }.map { it.clientName }

    /**
     * For this function we use a cascading call [mapCatching] to transform the result like in map but this way
     * we catch any exception thrown by the method in scope: [RemoteCheckerClient.clientAllowRemote]
     * and map it to a [Result.failure]
     */
    fun remoteClientExistForConsultant(consultant: Consultant): Result<Boolean> =
      runCatching {
        assignmentsDao.findBestMatchingAssignment(consultant)
      }.map { it.clientName }
        .mapCatching { remoteCheckerClient.clientAllowRemote(it) }

  }

  class AssignmentsDao {
    /**
     * Given a consultant, find the best matching Assignment.
     * Find a way the best way handle the case where no assignment is found
     */
    fun findBestMatchingAssignment(consultant: Consultant): Assignment =
      ASSIGNMENTS_DB.filter { assignment ->
        assignment.stack.any{ consultant.skills.contains(it) }
      }.maxByOrNull { assignment ->
        assignment.stack.count { consultant.skills.contains(it) }
      } ?: throw NoSuchElementException("not found")

  }

  @JvmStatic
  fun main(args: Array<String>) {
    val matchingService = MatchingService()
    val c1 = Consultant("Uncle Bob", setOf("c++"))
    val c2 = Consultant("Tony Hoare", setOf("java","spring"))

    /**
     * .fold() allow us to deal explicitly with success and failure cases.
     * Issues: we might miss easily a case in the when clause
     * because [Result.Failure] is of type [Throwable] (very general)
     * so we can't leverage the compiler
    */
    matchingService.remoteClientExistForConsultant(c2)
      .fold(
        onSuccess = { println("there is at least one client that allow remote work for consultant ${c2.name}") },
        onFailure = {
          when (it) {
            is IOException -> println("an IO error occurred: $it")
            is NoSuchElementException -> println("No client match the skills of the candidate: $it")
          }
        }
      )

    println("Consultant ${c1.name} is best assigned to client: ${matchingService.findBestMatchingClient(c1)}")
  }




  /** nb. You can define a custom [Result] type using sealed classes rather than using Result from Kotlin stdlib
   * If you want to have more control over the Failure case for example, but you have to implement yourself all the
   * monads methods: map, mapCatching, getOrElse, getCatching etc...
   */
  sealed class MyResult<out T> {
    data class Success<T>(val value: T) : MyResult<T>()
    data class Failure(val exception: Throwable, val message:String) : MyResult<Nothing>()
  }

}

