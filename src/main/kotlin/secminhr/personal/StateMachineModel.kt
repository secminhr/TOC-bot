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