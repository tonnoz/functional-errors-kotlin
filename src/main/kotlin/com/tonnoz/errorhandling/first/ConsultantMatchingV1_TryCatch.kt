package com.tonnoz.errorhandling.first

import java.util.NoSuchElementException

/*** Simple implementation with try catch in [MatchingService.findBestMatchingClient] (halt the program) */

object ConsultantMatchingV1_TryCatch {

  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

  class AssignmentsDao {

    fun findBestMatchingAssignment(consultant: Consultant): Assignment =
      ASSIGNMENTS_DB.filter { it.stack.any { skill -> consultant.skills.contains(skill) }} //filter out assignments that don't match any skill
        .maxByOrNull { it.stack.intersect(consultant.skills).size } //get the assignment with the most amount of matching skills
        ?: throw NoSuchElementException("No matching assignment found") // or throw exception if no assignments have been found
  }

  class MatchingService(private val assignmentsDao: AssignmentsDao = AssignmentsDao()) {
    fun findBestMatchingClient(consultant: Consultant): String {
      return try {
        val assignment = assignmentsDao.findBestMatchingAssignment(consultant) //No referential transparency
        assignment.clientName
      } catch (e: NoSuchElementException) {
        println(e)
        "No client found"
      }
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val c1 = Consultant("Tony Hoare", setOf("C++"))
    val c2 = Consultant("Uncle Bob", setOf("spring", "java"))
    val matchingService = MatchingService()
    println("Consultant ${c1.name} is best assigned to client: ${matchingService.findBestMatchingClient(c1)}")
    println("Consultant ${c2.name} is best assigned to client: ${matchingService.findBestMatchingClient(c2)}")
  }

}


