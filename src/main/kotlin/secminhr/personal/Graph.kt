package secminhr.personal

import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.toGraphviz
import java.io.File

fun main() {
    val g = graph(directed = true) {
        graph[Rank.dir(Rank.RankDir.LEFT_TO_RIGHT)]
        ("Start" - "Start")[Label.of("help")]
        ("Start" - "Running")[Label.of("run (machine available)")]
        ("Start" - "Start")[Label.of("run (no machine available)")]
        ("Running" - "Start")[Label.of("UserMachineExit")]
        ("Start" - "FirstInitState")[Label.of("new")]
        ("FirstInitState" - "Start")[Label.of("__cancel")]
        ("FirstInitState" - "Editing")[Label.of("ReceiveText")]
        ("Start" - "Editing")[Label.of("edit (machine available)")]
        ("Start" - "Start")[Label.of("edit (no machine available)")]
        ("Editing" - "ChangeInitStateName")[Label.of("change initial state")]
        ("ChangeInitStateName" - "Editing")[Label.of("__cancel")]
        ("ChangeInitStateName" - "ChangeInitStateName")[Label.of("ReceiveText (state doesn't exist)")]
        ("ChangeInitStateName" - "Editing")[Label.of("ReceiveText (state exists)")]
        ("Editing" - "StateCreate")[Label.of("state")]
        ("StateCreate" - "Editing")[Label.of("__cancel")]
        ("StateCreate" - "StateCreate")[Label.of("ReceiveText (state duplicated)")]
        ("StateCreate" - "Editing")[Label.of("ReceiveText (state valid)")]
        ("Editing" - "TransitionCreateFrom")[Label.of("transition")]
        ("TransitionCreateFrom" - "Editing")[Label.of("__cancel")]
        ("TransitionCreateFrom" - "TransitionCreateFrom")[Label.of("ReceiveText (state doesn't exist)")]
        ("TransitionCreateFrom" - "TransitionCreateTo")[Label.of("ReceiveText (state exists)")]
        ("TransitionCreateTo" - "Editing")[Label.of("__cancel")]
        ("TransitionCreateTo" - "TransitionCreateTo")[Label.of("ReceiveText (state doesn't exist")]
        ("TransitionCreateTo" - "TransitionCreateWhenReceive")[Label.of("ReceiveText (state exists)")]
        ("TransitionCreateWhenReceive" - "Editing")[Label.of("__cancel")]
        ("TransitionCreateWhenReceive" - "TransitionCreateAction")[Label.of("ReceiveText")]
        ("TransitionCreateAction" - "Editing")[Label.of("__cancel")]
        ("TransitionCreateAction" - "Editing")[Label.of("__none")]
        ("TransitionCreateAction" - "Editing")[Label.of("ReceiveText")]
        ("Editing" - "Start")[Label.of("done")]
        ("Start" - "Start")[Label.of("graph (no machine available)")]
        ("Start" - "GraphChooseFormat")[Label.of("graph (machine available)")]
        ("GraphChooseFormat" - "Start")[Label.of("svg")]
        ("GraphChooseFormat" - "Start")[Label.of("png")]
        ("GraphChooseFormat" - "Start")[Label.of("ReceiveText (unsupported format)")]
        "Start"[Shape.DOUBLE_CIRCLE, Style.FILLED, Color.RED, Color.rgb(221, 154, 127).fill()]
    }

    g.toGraphviz().render(Format.SVG).toFile(File("graph.svg"))
}