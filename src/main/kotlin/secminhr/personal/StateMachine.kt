package secminhr.personal

import com.tinder.StateMachine

enum class State {
    Start, Running, FirstInitState, ChangeInitStateName, SelectEditMachine, Editing, StateCreate,
    TransitionCreateFrom, TransitionCreateTo, TransitionCreateWhenReceive, TransitionCreateAction
}

sealed class Event {
    object NewMachine: Event()
    object EditMachine: Event()
    data class ReceiveText(val userId: String, val text: String): Event()
    object RenameCurrentNode: Event()
    object UserMachineExit: Event()
}

sealed class SideEffect {

}

fun userMachineNames(): List<String> {
    return listOf()
}

fun loadStateMachine(id: String): StateMachine<String, Any, Any> {
    return StateMachine.create {}
}

val servingMachines = mutableMapOf<String, StateMachine<*, Event, *>>()
val userTOCBackupMachine = mutableMapOf<String, StateMachine<*, Event, *>>()
val userCustomMachines = mutableMapOf<String, StateMachineModel>()

fun receiveFrom(user: String, text: String) = Event.ReceiveText(user, text)
fun quickReplies(vararg replies: String) = if (replies.isNotEmpty()) replies.toList() else emptyList()

fun TOCMachine(user: String) = StateMachine.create<State, Event, SideEffect> {
    initialState(State.Start)
    state(State.Start) {
        on(receiveFrom(user, "help")) {
            val replies = if (userCustomMachines.containsKey(user)) quickReplies("new", "edit", "run", "help") else quickReplies("new", "help")
            replyMessageTo(user,
                "Commands are listed at the bottom of the chat" to replies
            )
            transitionTo(State.Start)
        }
        on(receiveFrom(user, "new")) {
            userCustomMachines[user] = StateMachineModel(user)
            replyMessageTo(user,
                "Your machine is created" to quickReplies(),
                "Initial state name:" to quickReplies("__cancel")
            )
            transitionTo(State.FirstInitState)
        }
        on(receiveFrom(user, "edit")) {
            if (userCustomMachines.containsKey(user)) {
                replyMessageTo(user, "Start editing" to quickReplies("change initial state", "state", "transition", "done"))
                transitionTo(State.Editing)
            } else {
                replyMessageTo(user,
                    "You have no state machine" to quickReplies("new")
                )
                transitionTo(State.Start)
            }
        }
        on(receiveFrom(user, "run")) {
            if (userCustomMachines.containsKey(user)) {
                replyMessageTo(user, "Your machine is started, use __Exit when you want to exit")
                userTOCBackupMachine[user] = servingMachines[user]!!
                servingMachines[user] = userCustomMachines[user]!!.toStateMachine()
                transitionTo(State.Running)
            } else {
                replyMessageTo(user,
                    "You have no state machine" to quickReplies("new")
                )
                transitionTo(State.Start)
            }
        }
    }
    //new
    state(State.FirstInitState) {
        on(receiveFrom(user, "__cancel")) {
            replyMessageTo(user, "State machine creation is cancelled" to quickReplies("new", "help"))
            userCustomMachines.remove(user)
            transitionTo(State.Start)
        }
        on<Event.ReceiveText> {
            userCustomMachines[user]!!.states.add(it.text)
            userCustomMachines[user]!!.initialState = it.text
            replyMessageTo(user,
                "New state ${it.text} created" to quickReplies(),
                "State ${it.text} is now initial state" to quickReplies("change initial state", "state", "transition", "done")
            )
            transitionTo(State.Editing)
        }
    }
    //run
    state(State.Running) {
        on(Event.UserMachineExit) {
            replyMessageTo(user, "Exit" to quickReplies(), "Welcome back to edit mode" to quickReplies("new", "edit", "run", "help"))
            transitionTo(State.Start)
        }
    }

    //edit
    state(State.Editing) {
        on(receiveFrom(user, "change initial state")) {
            replyMessageTo(user, "Initial state name:" to quickReplies("__cancel"))
            transitionTo(State.ChangeInitStateName)
        }
        on(receiveFrom(user, "state")) {
            replyMessageTo(user, "New state name:" to quickReplies("__cancel"))
            transitionTo(State.StateCreate)
        }
        on(receiveFrom(user, "transition")) {
            replyMessageTo(user, "New transition from:" to quickReplies("__cancel"))
            transitionTo(State.TransitionCreateFrom)
        }
        on(receiveFrom(user, "done")) {
            replyMessageTo(user, "Done editing state machine" to quickReplies("new", "edit", "run", "help"))
            transitionTo(State.Start)
        }
    }
    state(State.ChangeInitStateName) {
        on(receiveFrom(user, "__cancel")) {
            replyMessageTo(user,
                "Initial state is left unchanged" to quickReplies("change initial state", "state", "transition", "done")
            )
            transitionTo(State.Editing)
        }
        on<Event.ReceiveText> {
            if (!userCustomMachines[user]!!.containsState(it.text)) {
                replyMessageTo(user,
                    "State ${it.text} doesn't exist" to quickReplies(),
                    "Initial state name:" to quickReplies("__cancel")
                )
                transitionTo(State.ChangeInitStateName)
            } else {
                userCustomMachines[user]!!.initialState = it.text
                replyMessageTo(user,
                    "State ${it.text} is now initial state" to quickReplies("change initial state", "state", "transition", "done")
                )
                transitionTo(State.Editing)
            }
        }
    }
    state(State.StateCreate) {
        on(receiveFrom(user, "__cancel")) {
            replyMessageTo(user, "State creation cancelled" to quickReplies("change initial state", "state", "transition", "done"))
            transitionTo(State.Editing)
        }
        on<Event.ReceiveText> {
            if (userCustomMachines[user]!!.states.contains(it.text)) {
                replyMessageTo(user,
                    "State named ${it.text} exists, states' names can't be duplicated" to quickReplies(),
                    "New state name:" to quickReplies("__cancel")
                )
                transitionTo(State.StateCreate)
            } else {
                userCustomMachines[user]!!.states.add(it.text)
                replyMessageTo(user, "State ${it.text} created" to quickReplies("change initial state", "state", "transition", "done"))
                transitionTo(State.Editing)
            }
        }
    }
    state(State.TransitionCreateFrom) {
        on(receiveFrom(user, "__cancel")) {
            replyMessageTo(user, "Transition creation cancelled" to quickReplies("change initial state", "state", "transition", "done"))
            userCustomMachines[user]!!.clearTransitionCreationArgs()
            transitionTo(State.Editing)
        }
        on<Event.ReceiveText> {
            if (userCustomMachines[user]!!.containsState(it.text)) {
                replyMessageTo(user, "To: " to quickReplies("__cancel"))
                userCustomMachines[user]!!.transitionCreationFromState = it.text
                transitionTo(State.TransitionCreateTo)
            } else {
                replyMessageTo(user,
                    "State ${it.text} doesn't exist" to quickReplies(),
                    "New transition from:" to quickReplies("__cancel")
                )
                transitionTo(State.TransitionCreateFrom)
            }
        }
    }
    state(State.TransitionCreateTo) {
        on(receiveFrom(user, "__cancel")) {
            replyMessageTo(user, "Transition creation cancelled" to quickReplies("change initial state", "state", "transition", "done"))
            userCustomMachines[user]!!.clearTransitionCreationArgs()
            transitionTo(State.Editing)
        }
        on<Event.ReceiveText> {
            if (userCustomMachines[user]!!.containsState(it.text)) {
                replyMessageTo(user, "When receive text: " to quickReplies("__cancel"))
                userCustomMachines[user]!!.transitionCreationToState = it.text
                transitionTo(State.TransitionCreateWhenReceive)
            } else {
                replyMessageTo(user, "State ${it.text} doesn't exist" to quickReplies(),
                    "To:" to quickReplies("__cancel")
                )
                transitionTo(State.TransitionCreateTo)
            }
        }
    }
    state(State.TransitionCreateWhenReceive) {
        on(receiveFrom(user, "__cancel")) {
            replyMessageTo(user, "Transition creation cancelled" to quickReplies("change initial state", "state", "transition", "done"))
            userCustomMachines[user]!!.clearTransitionCreationArgs()
            transitionTo(State.Editing)
        }
        on<Event.ReceiveText> {
            userCustomMachines[user]!!.transitionCreationWhenReceive = it.text
            replyMessageTo(user,
                "Send message while transitioning: Type __none if you don't want to send anything" to quickReplies("__cancel", "__none")
            )
            transitionTo(State.TransitionCreateAction)
        }
    }
    state(State.TransitionCreateAction) {
        on(receiveFrom(user, "__cancel")) {
            replyMessageTo(user, "Transition creation cancelled" to quickReplies("change initial state", "state", "transition", "done"))
            userCustomMachines[user]!!.clearTransitionCreationArgs()
            transitionTo(State.Editing)
        }
        on(receiveFrom(user, "__none")) {
            replyMessageTo(user, "Transition created" to quickReplies("change initial state", "state", "transition", "done"))
            userCustomMachines[user]!!.transitionCreationAction = SILENT
            userCustomMachines[user]!!.createTransition()
            transitionTo(State.Editing)
        }
        on<Event.ReceiveText> {
            replyMessageTo(user, "Transition created" to quickReplies("change initial state", "state", "transition", "done"))
            userCustomMachines[user]!!.transitionCreationAction = SEND_MSG(it.text)
            userCustomMachines[user]!!.createTransition()
            transitionTo(State.Editing)
        }
    }
}