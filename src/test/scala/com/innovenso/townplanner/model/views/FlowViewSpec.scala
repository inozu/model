package com.innovenso.townplanner.model.views

import com.innovenso.townplanner.model.concepts.properties.{Message, Request, Response}
import com.innovenso.townplanner.model.concepts.views.{CompiledFlowView, FlowView}
import com.innovenso.townplanner.model.concepts._
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec

class FlowViewSpec extends AnyFlatSpec with GivenWhenThen {
  "A flow view" should "have the correct number of container layers for each system context" in new EnterpriseArchitectureContext {
    Given("3 system contexts")
    val platform: ItPlatform = samples.platform()
    val system1: ItSystem = samples.system(
      withContainers = false,
      containingPlatform = Some(platform)
    )
    val microservice: Microservice = samples.microservice(system1)
    val database: Database = samples.database(system1)

    samples.flow(microservice, database)

    val system2: ItSystem = samples.system(
      withContainers = false,
      containingPlatform = Some(platform)
    )

    val ms2: Microservice = samples.microservice(system2)
    val ms3: Microservice = samples.microservice(system2)
    val db2: Database = samples.database(system2)

    samples.flow(ms2, database)
    samples.flow(ms3, database)
    samples.flow(ms2, microservice)

    val system3: ItSystem = samples.system(
      withContainers = false,
      containingPlatform = Some(platform)
    )

    val stream: Queue = samples.queue(system3)
    val ui1: WebUI = samples.ui(system3)
    val ms4: Microservice = samples.microservice(system3)

    samples.flow(ui1, ms4)
    samples.flow(ms4, stream)
    samples.flow(stream, ms3)
    samples.flow(stream, microservice)

    val query: Query = samples.query
    val tech1: Technology = samples.language
    val tech2: Technology = samples.framework

    When("a flow fiew is requested")
    val flowView: FlowView = ea needs FlowView(title = "The Flow View") and {
      that =>
        that has Request(
          "1"
        ) containing query using tech1 using tech2 from microservice to database
        that has Request(
          "2"
        ) containing query using tech2 from microservice to ms2
        that has Request("3") from microservice to ms3
        that has Request("4") from ms3 to ms4
        that has Request("5") from ms4 to stream
        that has Request("6") from ui1 to ms4
    }
    val compiledFlowView: CompiledFlowView = townPlan.flowView(flowView.key).get
    Then("each system has the correct number of containers")
    val containers1: Seq[ItContainer] = compiledFlowView.containers(system1)
    val containers2: Seq[ItContainer] = compiledFlowView.containers(system2)
    val containers3: Seq[ItContainer] = compiledFlowView.containers(system3)
    val layers1: Seq[ItContainerLayer] = compiledFlowView.layers(containers1)
    val layers2: Seq[ItContainerLayer] = compiledFlowView.layers(containers2)
    val layers3: Seq[ItContainerLayer] = compiledFlowView.layers(containers3)
    assert(containers1.size == 2)
    assert(containers2.size == 2)
    assert(containers3.size == 3)
    assert(layers1.size == 2)
    assert(layers2.size == 1)
    assert(layers3.size == 3)
    println(s"layers 1: ${layers1.map(_.title)}")
    println(s"layers 2: ${layers2.map(_.title)}")
    println(s"layers 3: ${layers3.map(_.title)}")
  }

  "A flow view" can "be added to the town plan" in new EnterpriseArchitectureContext {
    Given("some systems")
    val system1: ItSystem = ea has ItSystem(title = "A System")
    val system2: ItSystem = ea has ItSystem(title = "Another System")
    And("a user")
    val user: Actor = ea has Actor(title = "A user")
    And("a container")
    val container1: Microservice =
      ea describes Microservice(title = "A microservice") as { it =>
        it isPartOf system1
      }

    When("a flow view is requested")
    val flowView: FlowView = ea needs FlowView(title = "The Flow View") and {
      it =>
        it has Request("once ") from user to container1
        it has Request("upon ") from container1 to system2
        it has Response("a ") from system2 to container1
        it has Message("midnight ") from container1 to system1
        it has Response("dreary") from container1 to user
    }

    Then("it should exist")
    assert(exists(flowView))
    assert(townPlan.flowView(flowView.key).exists(_.containers.size == 1))
    assert(townPlan.flowView(flowView.key).exists(_.systemContexts.size == 1))
    assert(townPlan.flowView(flowView.key).exists(_.otherSystems.size == 1))
    assert(townPlan.flowView(flowView.key).exists(_.actorNouns.size == 1))

    And("the steps should be ordered")
    assert(
      townPlan
        .flowView(flowView.key)
        .exists(it =>
          it.view.interactions
            .map(_.name)
            .mkString == "once upon a midnight dreary"
        )
    )
  }
}
