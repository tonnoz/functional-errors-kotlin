package com.tonnoz.errorhandling.first

import java.util.NoSuchElementException

/*** Simple implementation with try catch in [MatchingService.findBestMatchingClient] (halt the program) */

object ConsultantMatchingV1 {

  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

  class AssignmentsDao {

    fun findBestMatchingAssignment(consultant: Consultant): Assignment {
      return ASSIGNMENTS_DB.filter { assignment -> //filter out assignments that don't match any skill
        assignment.stack.any { skill -> consultant.skills.contains(skill) }
      }.maxByOrNull { assignment -> //get the assignment with the most amount of matching skills
        assignment.stack.count { skill -> consultant.skills.contains(skill) } // or throw exception if no assignments have been found
      } ?: throw NoSuchElementException("There are no assignments matching the skills of ${consultant.name}")
    }
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


