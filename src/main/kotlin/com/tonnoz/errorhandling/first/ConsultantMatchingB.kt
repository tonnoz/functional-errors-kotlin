package com.tonnoz.errorhandling.first

/*** Let's build an app to match consultants to clients based on their skill set */
object ConsultantMatchingB {

  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )


  class MatchingService(private val assignmentsDao: AssignmentsDao = AssignmentsDao()) {
    /**
     * Given a consultant, find the best matching client (name) for them,
     * using the most closely matching skill set
     */
    fun findBestMatchingClient(consultant: Consultant): String {
     TODO()
    }
  }

  class AssignmentsDao {
    /**
     * Given a consultant, find the best matching Assignment.
     * Find a way the best way handle the case where no assignment is found
     */
    fun findBestMatchingAssignment(consultant: Consultant): Assignment {
      TODO()
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
   TODO()
  }

}


