package com.tonnoz.errorhandling

import arrow.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull

/*** In this variation, we make use of Either from Arrow core library to handle errors
 ***/
object ConsultantMatchingV5_Either_start {

  /***
   ***  we introduce a sealed interface [MatchingError] to represent the different types of errors that can occur
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


    fun remoteClientExistForConsultant(consultant: Consultant): Either<MatchingError, Boolean> = TODO()


    fun remoteClientExistForConsultantScoped(consultant: Consultant): Either<MatchingError, Boolean> = TODO()

  }


  @JvmStatic
  fun main(args: Array<String>) {
    val matchingService = MatchingService()
    val c1 = Consultant("Uncle Bob", setOf("c++"))
    val c2 = Consultant("Tony Hoare", setOf("java","spring"))

    val myEither = matchingService.remoteClientExistForConsultantScoped(c1)
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
