package secminhr.personal

import com.tinder.StateMachine
import guru.nidi.graphviz.engine.Format

enum class State {
    Start,
    Running,
    FirstInitState,
    Editing,
    ChangeInitStateName, StateCreate,
    TransitionCreateFrom, TransitionCreateTo, TransitionCreateWhenReceive, TransitionCreateAction,
    GraphChooseFormat,
}

sealed class Event {
    data class ReceiveText(val userId: String, val text: String): Event()
    object UserMachineExit: Event()
}

sealed class SideEffect {}

val servingMachines = mutableMapOf<String, StateMachine<*, Event, *>>()
val userTOCBackupMachine = mutableMapOf<String, StateMachine<*, Event, *>>()
val userCustomMachines = mutableMapOf<String, StateMachineModel>()

fun receiveFrom(user: String, text: String) = Event.ReceiveText(user, text)
fun quickReplies(vararg replies: String) = if (replies.isNotEmpty()) replies.toList() else emptyList()

fun State.getQuickReplies(user: String): List<String> = when (this) {
    State.Start -> {
        if (userCustomMachines.contains(user)) quickReplies("new", "edit", "run", "graph", "help")
        else quickReplies("new", "help")
    }
    State.Running -> emptyList()
    State.FirstInitState -> quickReplies("__cancel")
    State.Editing -> quickReplies("change initial state", "state", "transition", "done")
    State.ChangeInitStateName -> quickReplies("__cancel")
    State.StateCreate -> quickReplies("__cancel")
    State.TransitionCreateFrom -> quickReplies("__cancel")
    State.TransitionCreateTo -> quickReplies("__cancel")
    State.TransitionCreateWhenReceive -> quickReplies("__cancel")
    State.TransitionCreateAction -> quickReplies("__cancel", "__none")
    State.GraphChooseFormat -> quickReplies("png", "svg")
}

val domain by lazy {
    System.getenv("DOMAIN")
}
fun TOCMachine(user: String) = StateMachine.create<State, Event, SideEffect> {
    initialState(State.Start)
    state(State.Start) {
        on(receiveFrom(user, "help")) {
            replyAndTransition(user, State.Start, "Commands are listed at the bottom of the chat")
        }
        on(receiveFrom(user, "new")) {
            userCustomMachines[user] = StateMachineModel(user)
            replyAndTransition(user, State.FirstInitState, "Your machine is created", "Initial state name:")
        }
        on(receiveFrom(user, "edit")) {
            if (userCustomMachines.contains(user)) {
                replyAndTransition(user, State.Editing, "Start editing")
            } else {
                replyAndTransition(user, State.Start, "You have no state machine")
            }
        }
        on(receiveFrom(user, "run")) {
            if (userCustomMachines.containsKey(user)) {
                replyMessageTo(user, "Your machine is started, use __Exit when you want to exit")
                userTOCBackupMachine[user] = servingMachines[user]!!
                servingMachines[user] = userCustomMachines[user]!!.toStateMachine()
                transitionTo(State.Running)
            } else {
                replyAndTransition(user, State.Start, "You have no state machine")
            }
        }
        on(receiveFrom(user, "graph")) {
            if (userCustomMachines.contains(user)) {
                replyAndTransition(user, State.GraphChooseFormat, "Choose image format:")
            } else {
                replyAndTransition(user, State.Start, "You have no state machine")
            }
        }
    }
    //new
    state(State.FirstInitState) {
        on(receiveFrom(user, "__cancel")) {
            userCustomMachines.remove(user)
            replyAndTransition(user, State.Start, "State machine creation is cancelled")
        }
        on<Event.ReceiveText> {
            userCustomMachines[user]!!.states.add(it.text)
            userCustomMachines[user]!!.initialState = it.text
            replyAndTransition(user, State.Editing, "New state ${it.text} created", "State ${it.text} is now initial state")
        }
    }
    //run
    state(State.Running) {
        on(Event.UserMachineExit) {
            replyAndTransition(user, State.Start, "Exit", "Welcome back to edit mode")
        }
    }

    //edit
    state(State.Editing) {
        on(receiveFrom(user, "change initial state")) {
            replyAndTransition(user, State.ChangeInitStateName, "Initial state name:")
        }
        on(receiveFrom(user, "state")) {
            replyAndTransition(user, State.StateCreate, "New state name:")
        }
        on(receiveFrom(user, "transition")) {
            replyAndTransition(user, State.TransitionCreateFrom, "New transition from:")
        }
        on(receiveFrom(user, "done")) {
            replyAndTransition(user, State.Start, "Done editing state machine")
        }
    }
    state(State.ChangeInitStateName) {
        on(receiveFrom(user, "__cancel")) {
            replyAndTransition(user, State.Editing, "Initial state is left unchanged")
        }
        on<Event.ReceiveText> {
            if (!userCustomMachines[user]!!.containsState(it.text)) {
                replyAndTransition(user, State.ChangeInitStateName, "State ${it.text} doesn't exist", "Initial state name:")
            } else {
                userCustomMachines[user]!!.initialState = it.text
                replyAndTransition(user, State.Editing, "State ${it.text} is now initial state")
            }
        }
    }
    state(State.StateCreate) {
        on(receiveFrom(user, "__cancel")) {
            replyAndTransition(user, State.Editing, "State creation cancelled")
        }
        on<Event.ReceiveText> {
            if (userCustomMachines[user]!!.states.contains(it.text)) {
                replyAndTransition(user, State.StateCreate, "State named ${it.text} exists, states' names can't be duplicated", "New state name:")
            } else {
                userCustomMachines[user]!!.states.add(it.text)
                replyAndTransition(user, State.Editing, "State ${it.text} created")
            }
        }
    }
    state(State.TransitionCreateFrom) {
        on(receiveFrom(user, "__cancel")) {
            userCustomMachines[user]!!.clearTransitionCreationArgs()
            replyAndTransition(user, State.Editing, "Transition creation cancelled")
        }
        on<Event.ReceiveText> {
            if (userCustomMachines[user]!!.containsState(it.text)) {
                userCustomMachines[user]!!.transitionCreationFromState = it.text
                replyAndTransition(user, State.TransitionCreateTo, "To:")
            } else {
                replyAndTransition(user, State.TransitionCreateFrom, "State ${it.text} doesn't exist", "New transition from:")
            }
        }
    }
    state(State.TransitionCreateTo) {
        on(receiveFrom(user, "__cancel")) {
            userCustomMachines[user]!!.clearTransitionCreationArgs()
            replyAndTransition(user, State.Editing, "Transition creation cancelled")
        }
        on<Event.ReceiveText> {
            if (userCustomMachines[user]!!.containsState(it.text)) {
                userCustomMachines[user]!!.transitionCreationToState = it.text
                replyAndTransition(user, State.TransitionCreateWhenReceive, "When receive text:")
            } else {
                replyAndTransition(user, State.TransitionCreateTo, "State ${it.text} doesn't exist", "To:")
            }
        }
    }
    state(State.TransitionCreateWhenReceive) {
        on(receiveFrom(user, "__cancel")) {
            userCustomMachines[user]!!.clearTransitionCreationArgs()
            replyAndTransition(user, State.Editing, "Transition creation cancelled")
        }
        on<Event.ReceiveText> {
            userCustomMachines[user]!!.transitionCreationWhenReceive = it.text
            replyAndTransition(user, State.TransitionCreateAction, "Send message while transitioning: Type __none if you don't want to send anything")
        }
    }
    state(State.TransitionCreateAction) {
        on(receiveFrom(user, "__cancel")) {
            replyAndTransition(user, State.Editing, "Transition creation cancelled")
        }
        on(receiveFrom(user, "__none")) {
            userCustomMachines[user]!!.transitionCreationAction = SILENT
            userCustomMachines[user]!!.createTransition()
            replyAndTransition(user, State.Editing, "Transition created")
        }
        on<Event.ReceiveText> {
            userCustomMachines[user]!!.transitionCreationAction = SEND_MSG(it.text)
            userCustomMachines[user]!!.createTransition()
            replyAndTransition(user, State.Editing, "Transition created")
        }
    }
    //graph
    state(State.GraphChooseFormat) {
        onEnter {
            userCustomMachines[user]!!.createGraph()
        }
        on<Event.ReceiveText> {
            val message = if (it.text == "png" || it.text == "svg") {
                val file = userCustomMachines[user]!!.saveGraph(if (it.text == "png") Format.PNG  else Format.SVG)
                "View the image: ${domain}/images/${file.name}"
            } else {
                "unsupported format"
            }
            replyAndTransition(user, State.Start, message)
        }
    }
}

context(StateMachine.GraphBuilder<State, Event, SideEffect>.StateDefinitionBuilder<State>)
fun State.replyAndTransition(userId: String, state: State, vararg messages: String): StateMachine.Graph.State.TransitionTo<State, SideEffect> {
    val packedMessages = messages.mapIndexed { index, msg ->
        msg to if (index == messages.lastIndex) state.getQuickReplies(userId) else emptyList()
    }.toTypedArray()
    replyMessageTo(userId, *packedMessages)

    return transitionTo(state)
}
