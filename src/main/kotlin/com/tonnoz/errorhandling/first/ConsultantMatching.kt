package com.tonnoz.errorhandling.first


/*** Let's build an app to match consultants to clients based on their skill set */
object ConsultantMatching {

  data class Assignment(val name: String, val stack: Set<String>, val clientName: String)
  data class Consultant(val name: String, val skills: Set<String>)

  val ASSIGNMENTS_DB: Set<Assignment> = setOf(
    Assignment("Assignment aviation", setOf("java", "spring", "kafka"), "Aviation client"),
    Assignment("Assignment banking", setOf("kotlin", "spring", "angular"), "Banking client"),
    Assignment("Assignment e-commerce", setOf("kotlin", "ktor", "react"), "E-commerce client"),
  )

  class AssignmentsDao {
    /**
     * Given a consultant, find the best matching Assignment
     * by comparing the max number of matching skills of
     * the consultant to the assignment needed skills
     */
    fun findBestMatchingAssignment(consultant: Consultant): Assignment {
      TODO()
    }
  }

  class MatchingService(private val assignmentsDao: AssignmentsDao = AssignmentsDao()) {
    /**
     * Given a consultant, find the best matching client (name) for them
     */
    fun findBestMatchingClient(consultant: Consultant): String {
      TODO()
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

