package com.tonnoz.errorhandling

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.NoSuchElementException

/*** In this variation, we make use of Either from Arrow core library to handle errors
 ***/
object ConsultantMatchingV5_Either {



  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>, val remoteOnly: Boolean = false)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

  class AssignmentsDao {
    fun findBestMatchingAssignment(consultant: Consultant): Assignment =
    ASSIGNMENTS_DB.filter { assignment ->
      assignment.stack.any { skill -> consultant.skills.contains(skill) }
    }.maxByOrNull { assignment ->
      assignment.stack.intersect(consultant.skills).size
    } ?: throw NoSuchElementException("No matching assignment found")
}


  class RemoteEmployeeCheckerClient {

    /** (fake) RPC that returns true if {clientName} accepts remote working, false otherwise.
     * But! 1 time out of 3 the remote service is down...
     * */
    fun acceptRemoteDevs(clientName: String): Boolean = runBlocking{
      val random = (0..3).random()
      if (random == 0) throw IOException("Remote service is down")
      print("Calling remote service for clientName=[$clientName]...")
      delay(1500)
      println("DONE!")
      clientName === "Aviation client"
    }
  }



  class MatchingService(
    private val assignmentsDao: AssignmentsDao = AssignmentsDao(),
    private val remoteEmployeeCheckerClient: RemoteEmployeeCheckerClient = RemoteEmployeeCheckerClient()
  ) {

    /**
     * first version: simple call with no check for remote client
     */
    fun findBestMatchingClient(consultant: Consultant): Result<String> =
       runCatching {
        assignmentsDao.findBestMatchingAssignment(consultant)
      }.map { it.clientName  }

    /**
     * second version: if any exception is thrown within the runCatching block
     * it will be caught and the function will return a Result.failure with
     * that exception. This means that if the first operation fails,
     * the second operation won't be executed at all.
     */
    fun findBestMatchingClientR(consultant: Consultant): Result<String> =
      runCatching { // this behaves similar to a try/catch but with a Result return type
        val assignment = assignmentsDao.findBestMatchingAssignment(consultant)
        val remoteFriendly = remoteEmployeeCheckerClient.acceptRemoteDevs(assignment.clientName)
        if (consultant.remoteOnly && !remoteFriendly) "Client ${assignment.clientName} is a match but is not remote friendly"
        else assignment.clientName
      }

    /**
     * third version: runCatching only on first call then mapCatching and fold
     */
    fun findBestMatchingClientR2(consultant: Consultant): Result<String> {
      val assignmentResult = runCatching {
        assignmentsDao.findBestMatchingAssignment(consultant)
      }
      val remoteFriendlyResult = assignmentResult.mapCatching {
        remoteEmployeeCheckerClient.acceptRemoteDevs(it.clientName)
      }
      return remoteFriendlyResult.fold(
        onSuccess = { isRemoteFriendly ->
          if (consultant.remoteOnly && !isRemoteFriendly) Result.success("Client ${assignmentResult.getOrNull()?.clientName} is a match but is not remote friendly")
          else assignmentResult.map { it.clientName }
        },
        onFailure = { Result.failure(it) }
      )
    }

  }

  @JvmStatic
  fun main(args: Array<String>) {
    val c1 = Consultant("Uncle Bob", setOf("spring", "java"), true)
    val c2 = Consultant("Thomas Anderson", setOf("kotlin", "angular"), true)
    val c3 = Consultant("Tony Hoare", setOf("C++"))
    val service = MatchingService()
    println("Consultant [${c1.name}] is best assigned to client: [${service.findBestMatchingClientR2(c1)}]")
    println("Consultant [${c2.name}] is best assigned to client: [${service.findBestMatchingClientR2(c2)}]")
    println("Consultant [${c3.name}] is best assigned to client: [${service.findBestMatchingClientR2(c3)}]")
  }

}

