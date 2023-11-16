package com.tonnoz.errorhandling

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.NoSuchElementException

/*** In this variation, we revert to map logical failure with Exceptions
 ** in [AssignmentsDao.findBestMatchingAssignment] but we use the
 ** [Result] class as a wrapper, applying a more functional approach in [MatchingService] and in [main]
 *  **/
object ConsultantMatchingV4_Result_start {

  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

  class AssignmentsDao {
    /**
     * Given a consultant, find the best matching Assignment.
     * Find a way the best way handle the case where no assignment is found
     */
    fun findBestMatchingAssignment(consultant: Consultant): Assignment =
      ASSIGNMENTS_DB.filter { assignment ->
        assignment.stack.any { skill -> consultant.skills.contains(skill) }
      }.maxByOrNull { assignment ->
        assignment.stack.intersect(consultant.skills).size
      } ?: throw NoSuchElementException("No matching assignment found")

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

    fun findBestMatchingClient(consultant: Consultant): Result<String> = TODO()


    fun remoteClientExistForConsultant(consultant: Consultant): Result<Boolean> = TODO()


  }

  @JvmStatic
  fun main(args: Array<String>) {
    val matchingService = MatchingService()
    val c1 = Consultant("Uncle Bob", setOf("c++"))
    val c2 = Consultant("Tony Hoare", setOf("java","spring"))


    //we can use different ways to handle a Result:


    val result = matchingService.remoteClientExistForConsultant(c2)
      .onSuccess { println("there is at least one client that allow remote work for consultant ${c2.name}") }
      .onFailure {   when (it) {
            is IOException -> println("an IO error occurred: $it")
            is NoSuchElementException -> println("No client match the skills of the candidate: $it")
          }}


    /** We can try to recover from a failure with [recover] but watch out if an exception is thrown in the recover block
    *   it will be propagated to the caller. */
    val stillAResult = matchingService.remoteClientExistForConsultant(c2)
      .recover {
        when (it) {
          is IOException -> println("an IO error occurred: $it")
          is NoSuchElementException -> println("No client match the skills of the candidate: $it")
          else -> println("Unknown error: $it")
        }
        false
      }

    /** instead with [recoverCatching] if an exception is thrown in the recover block, it will be wrapped in a [Result.failure] **/
    val stillAResult1 = matchingService.remoteClientExistForConsultant(c2)
      .recoverCatching {
        when (it) {
          is IOException -> println("an IO error occurred: $it")
          is NoSuchElementException -> println("No client match the skills of the candidate: $it")
          else -> println("Unknown error: $it")
        }
        false
      }


    /**
     * .fold() allow us to deal explicitly with success and failure cases.
     * Issue: we might miss easily an Exception case in the when clause
     * because [Result.Failure] is of type [Throwable] which is not a sealed class
    */
    val nothing = matchingService.remoteClientExistForConsultant(c2)
      .fold(
        onSuccess = { println("there is at least one client that allow remote work for consultant ${c2.name}") },
        onFailure = {
          when (it) {
            is IOException -> println("an IO error occurred: $it")
            is NoSuchElementException -> println("No client match the skills of the candidate: $it")
          }
        }
      )
  }

}

