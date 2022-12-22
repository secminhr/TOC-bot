package secminhr.personal

import com.tinder.StateMachine

fun interface Action {
    fun run(userId: String)
}

val SILENT = Action {
    //no action
}
fun SEND_MSG(msg: String) = Action {
    replyMessageTo(it, msg)
}

val venderMachineInitalState = "Start"
val venderMachineStates = mutableSetOf("Start", "$0", "$5", "$10", "$15", "$20", "$25")
val venderMachineTransitions = mutableMapOf(
    "Start" to mutableListOf(
        Triple("buy", "$0", SEND_MSG("Remain: $20"))
    ),
    "$0" to mutableListOf(
        Triple("5", "$5", SEND_MSG("Remain: $15")),
        Triple("10", "$10", SEND_MSG("Remain: $10"))
    ),
    "$5" to mutableListOf(
        Triple("5", "$10", SEND_MSG("Remain: $10")),
        Triple("10", "$15", SEND_MSG("Remain: $5"))
    ),
    "$10" to mutableListOf(
        Triple("5", "$15", SEND_MSG("Remain: $5")),
        Triple("10", "$20", SEND_MSG("Here's your drink"))
    ),
    "$15" to mutableListOf(
        Triple("5", "$20", SEND_MSG("Here's your drink")),
        Triple("10", "$25", SEND_MSG("Here's your drink and $5 in change"))
    ),
    "$20" to mutableListOf(
        Triple("buy", "$0", SEND_MSG("Remain: $20"))
    ),
    "$25" to mutableListOf(
        Triple("buy", "$0", SEND_MSG("Remain: $20"))
    )
)

fun createVenderMachine(ownerUserId: String) =
    StateMachineModel(ownerUserId, venderMachineInitalState, venderMachineStates, venderMachineTransitions).toStateMachine()


data class StateMachineModel(
    val ownerUserId: String,
    var initialState: String = "",
    val states: MutableSet<String> = mutableSetOf(),
    //transitions structure: Key = start state, value = (event text, end state, action)
    val transitions: MutableMap<String, MutableList<Triple<String, String, Action>>> = mutableMapOf()
) {
    var transitionCreationFromState: String = ""
    var transitionCreationToState: String = ""
    var transitionCreationWhenReceive: String = ""
    var transitionCreationAction: Action? = null

    fun containsState(state: String) = states.contains(state)
    fun createTransition() {
        //the validation of the arguments should be done outside, before each argument assignment
        val transitionsArg = Triple(transitionCreationWhenReceive, transitionCreationToState, transitionCreationAction ?: SILENT)
        transitions.putIfAbsent(transitionCreationFromState, mutableListOf())
        transitions[transitionCreationFromState]!!.add(transitionsArg)
        clearTransitionCreationArgs()
    }
    fun clearTransitionCreationArgs() {
        transitionCreationFromState = ""
        transitionCreationToState = ""
        transitionCreationWhenReceive = ""
        transitionCreationAction = null
    }

    fun toStateMachine(): StateMachine<String, Event, Any> {
        return StateMachine.create {
            initialState(initialState)
            for (machineState in states) {
                state("__Exiting") {
                    onEnter {
                        servingMachines[ownerUserId] = userTOCBackupMachine[ownerUserId]!!
                        servingMachines[ownerUserId]!!.transition(Event.UserMachineExit)
                    }
                }

                state(machineState) {
                    on(receiveFrom(ownerUserId, "__Exit")) {
                        return@on transitionTo("__Exiting")
                    }
                    transitions[machineState]?.forEach { (event, next, action) ->
                        on(receiveFrom(ownerUserId, event)) {
                            action.run(it.userId)
                            return@on transitionTo(next)
                        }
                    }
                }
            }
        }
    }
}