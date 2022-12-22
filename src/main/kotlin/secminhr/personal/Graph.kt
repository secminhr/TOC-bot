package secminhr.personal

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.toGraphviz
import java.io.File

fun main() {
    val g = graph(directed = true) {
        ("Start" - "Start")[Label.of("help   ")]
        ("Start" - "Start")[Label.of("run")]
        "Start"[Shape.DOUBLE_CIRCLE, Style.FILLED, Color.RED, Color.rgb(221, 154, 127).fill()]
    }

    g.toGraphviz().render(Format.SVG).toFile(File("graph.svg"))
}