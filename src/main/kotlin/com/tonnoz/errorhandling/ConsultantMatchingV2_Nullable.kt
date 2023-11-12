package com.tonnoz.errorhandling

/*** In this variation we introduce nullable types (?) as an alternative to exceptions
 ** in [AssignmentsDao.findBestMatchingAssignment]
 ***/
object ConsultantMatchingV2_Nullable {

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
        assignment.stack.count { skill -> consultant.skills.contains(skill) }
      }
    }
  }

  class MatchingService(private val assignmentsDao: AssignmentsDao = AssignmentsDao()) {
    fun findBestMatchingClient(consultant: Consultant): String {
      val assignment = assignmentsDao.findBestMatchingAssignment(consultant)
      // referential transparency is maintained, but now we have to handle nullability manually
      // We also lost the context of the error (type of error)
      return assignment?.clientName ?: "No client found"
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val c1 = Consultant("Uncle Bob", setOf("spring", "java"))
    val c2 = Consultant("Tony Hoare", setOf("C++"))
    val matchingService = MatchingService()
    println("Consultant ${c1.name} is best assigned to client: ${matchingService.findBestMatchingClient(c1)}")
    println("Consultant ${c2.name} is best assigned to client: ${matchingService.findBestMatchingClient(c2)}")
  }

}


