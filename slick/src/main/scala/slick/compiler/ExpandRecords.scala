package slick.compiler

import slick.ast._
import Util._

/** Expand paths of record types to reference all fields individually and
  * recreate the record structure at the call site. */
class ExpandRecords extends Phase {
  val name = "expandRecords"

  def apply(state: CompilerState) =
    state.map(_.replace({ case n @ Path(_) => expandPath(n) }, keepType = true))

  def expandPath(n: Node): Node = n.nodeType.structural match {
    case StructType(ch) =>
      StructNode(ch.map { case (s, t) =>
        (s, expandPath(n.select(s).nodeTypedOrCopy(t)))
      }(collection.breakOut)).nodeTyped(n.nodeType)
    case p: ProductType =>
      ProductNode(p.numberedElements.map { case (s, t) =>
        expandPath(n.select(s).nodeTypedOrCopy(t))
      }.toVector).nodeTyped(n.nodeType)
    case t => n
  }
}