package com.tonnoz.errorhandling.first

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException

/*** In this variation, we introduce a new class called [RemoteEmployeeCheckerClient]
 ** that simulate a Remote call (e.g. REST call) to check whether a given client supports
 ** consultants working from remote. **/
object ConsultantMatchingV3_CascadingCall {

  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

  class AssignmentsDao {
    fun findBestMatchingAssignment(consultant: Consultant): Assignment? {
      return ASSIGNMENTS_DB.filter { assignment ->
        assignment.stack.any { skill -> consultant.skills.contains(skill) }
      }.maxByOrNull { assignment ->
        assignment.stack.intersect(consultant.skills).size
      }
    }
  }


  class RemoteEmployeeCheckerClient {

    /** (fake) remote call that returns true if [clientName] accepts remote working, false otherwise.
     * But! 1 time out of 3 the remote service is down...
     * */
    fun acceptRemoteDevs(clientName: String): Boolean = runBlocking{
      val random = (0..2).random()
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
     * Given a consultant, find the best matching client (name) for them,
     * using the most closely matching skill set, return "No client found" if none is found
     */
    fun findBestMatchingClient(consultant:Consultant): String =
      assignmentsDao.findBestMatchingAssignment(consultant)?.clientName ?: "No client found"

    /**
     * With an extra (cascading) call that can fail for network reasons,
     * we notice some new issues :
     * - The coding style become very defensive against nulls
     * - We have again side effects due to the IOException in acceptRemoteDevs that
     *   halts the whole flow again
     */
    fun remoteClientExistForConsultant(consultant: Consultant): Boolean =
      assignmentsDao.findBestMatchingAssignment(consultant)?.clientName
      ?.let { remoteEmployeeCheckerClient.acceptRemoteDevs(it) } ?: false

  }

  @JvmStatic
  fun main(args: Array<String>) {
    val c1 = Consultant("Uncle Bob", setOf("spring", "java"))
    val c2 = Consultant("Tony Hoare", setOf("C++"))
    val matchingService = MatchingService()
    println("remote client for ${c2.name} = ${matchingService.remoteClientExistForConsultant(c2)}")
  }

}


